package com.ezbuy.ezbuy.services.impl;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ezbuy.ezbuy.dtos.request.CreateOrderRequest;
import com.ezbuy.ezbuy.dtos.request.UpdateOrderStatusRequest;
import com.ezbuy.ezbuy.dtos.response.AdminOrderSummaryResponse;
import com.ezbuy.ezbuy.dtos.response.OrderResponse;
import com.ezbuy.ezbuy.dtos.response.OrderSummaryResponse;
import com.ezbuy.ezbuy.dtos.response.PageResponse;
import com.ezbuy.ezbuy.entities.*;
import com.ezbuy.ezbuy.enums.OrderStatus;
import com.ezbuy.ezbuy.enums.PaymentStatus;
import com.ezbuy.ezbuy.exceptions.NotFoundException;
import com.ezbuy.ezbuy.mappers.OrderMapper;
import com.ezbuy.ezbuy.repositories.*;
import com.ezbuy.ezbuy.repositories.PaymentTransactionRepository;
import com.ezbuy.ezbuy.repositories.specifications.OrderSpecification;
import com.ezbuy.ezbuy.services.CartCleanupService;
import com.ezbuy.ezbuy.services.OrderService;
import com.ezbuy.ezbuy.services.VNPayService;

import com.ezbuy.ezbuy.enums.StockStrategyType;
import com.ezbuy.ezbuy.strategies.stock.StockDeductionContext;

import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final PaymentRepository paymentRepository;
    private final PromotionRepository promotionRepository;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final CartCleanupService cartCleanupService;
    private final VNPayService vnpayService;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final StockDeductionContext stockDeductionContext;
    private final CacheManager cacheManager;

    @Value("${vnpay.currency:VND}")
    private String vnpayCurrency = "VND";

    private static final BigDecimal EXCHANGE_RATE = new BigDecimal("25300");

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, HttpServletRequest httpRequest) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        int originalCartItemCount = cartRepository.countByUser(currentUser);
        if (originalCartItemCount == 0) {
            throw new IllegalStateException("Cart is empty.");
        }

        List<Cart> validItems = cartCleanupService.cleanCartAndGetValidItems(currentUser);

        if (validItems.size() != originalCartItemCount) {
            throw new IllegalStateException(
                "Some products in your cart were unavailable and have been removed. " +
                "Please review your updated cart before proceeding to checkout."
            );
        }

        Payment payment = paymentRepository.findById(request.getPaymentId()).orElseThrow(() -> new NotFoundException("Payment method not found."));
        Order newOrder = Order.builder()
                .user(currentUser)
                .receiverName(request.getReceiverName())
                .shippingAddress(request.getShippingAddress())
                .phone(request.getPhone())
                .note(request.getNote())
                .status(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.UNPAID)
                .payment(payment)
                .build();

        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        for (Cart cartItem : validItems) {
            Product product = cartItem.getProduct();
            OrderItem orderItem = OrderItem.builder()
                    .order(newOrder)
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .price(product.getPrice()) 
                    .productName(product.getName()) 
                    .build();
            orderItems.add(orderItem);
            subtotal = subtotal.add(product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }
        newOrder.setOrderItems(orderItems);
        newOrder.setSubtotal(subtotal);

        BigDecimal discountTotal = BigDecimal.ZERO;
        if (request.getPromoCode() != null && !request.getPromoCode().isEmpty()) {
            Promotion promotion = promotionRepository.findByCode(request.getPromoCode())
                    .orElseThrow(() -> new NotFoundException("Promotion code is not valid."));

            if (promotion.getEndDate().isBefore(LocalDateTime.now())) {
                throw new IllegalStateException("Promotion code has expired.");
            }

            discountTotal = subtotal.multiply(promotion.getDiscountValue().divide(new BigDecimal(100)));
            newOrder.setPromotion(promotion);
        }
        newOrder.setDiscountTotal(discountTotal);

        newOrder.setTaxTotal(BigDecimal.ZERO);
        newOrder.setTotalAmount(subtotal.subtract(discountTotal));
        
        Order savedOrder = orderRepository.save(newOrder);

        // Deduct stock using the resolved strategy (Header X-Stock-Strategy, param, or default)
        StockStrategyType strategyType = stockDeductionContext.resolveStrategyType(httpRequest);
        stockDeductionContext.deductStock(strategyType, savedOrder.getOrderItems());
        evictProductCaches(savedOrder.getOrderItems());
        
        cartRepository.deleteByUser(currentUser);

        String paymentUrl = null;
        
        if (payment.getMethod().equalsIgnoreCase("VNPAY")) {
            BigDecimal totalAmountUSD = savedOrder.getTotalAmount();
            long amount = "USD".equalsIgnoreCase(vnpayCurrency)
                    ? totalAmountUSD.longValue()
                    : totalAmountUSD.multiply(EXCHANGE_RATE).longValue();
            
            String vnp_TxnRef = savedOrder.getId() + "_" + System.currentTimeMillis();
            paymentUrl = vnpayService.createPaymentUrl(
                    amount, 
                    "Pay for order " + savedOrder.getId(), 
                    vnp_TxnRef, 
                    httpRequest
            );
        }
        OrderResponse response = orderMapper.toOrderResponse(savedOrder);
        response.setPaymentUrl(paymentUrl);

        return response;
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatusByAdmin(Integer orderId, UpdateOrderStatusRequest request) {
        User admin = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found with id: " + orderId));
        
        OrderStatus newStatus = request.getStatus();
        OrderStatus currentStatus = order.getStatus();

        if (currentStatus == OrderStatus.COMPLETED || currentStatus == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot change status of a completed or cancelled order.");
        }

        if (newStatus == OrderStatus.CANCELLED) {
            order.setStatus(OrderStatus.CANCELLED);
            restoreProductStock(order);
        } else {
            switch (newStatus) {
                case CONFIRMED:
                    if (currentStatus != OrderStatus.PENDING) throw new IllegalStateException("Order must be PENDING to be CONFIRMED.");
                    if (currentStatus != OrderStatus.PENDING) {
                        throw new IllegalStateException("Order must be PENDING to be CONFIRMED.");
                    }

                    boolean isVnpay = "VNPAY".equalsIgnoreCase(order.getPayment().getMethod());
                    boolean isNotPaid = order.getVnpTransactionNo() == null || order.getPaymentStatus() != PaymentStatus.PAID;

                    if (isVnpay && isNotPaid) {
                        throw new IllegalStateException("VNPAY order not yet paid (Missing Transaction No.). Cannot be confirmed!");
                    }
                    order.setConfirmedBy(admin);
                    order.setConfirmedAt(LocalDateTime.now());
                    break;
                case SHIPPING:
                    if (currentStatus != OrderStatus.CONFIRMED) throw new IllegalStateException("Order must be CONFIRMED to be SHIPPED.");
                    break;
                case COMPLETED:
                    if (currentStatus != OrderStatus.SHIPPING) throw new IllegalStateException("Order must be SHIPPING to be COMPLETED.");
                    break;
                default:
                    throw new IllegalStateException("Invalid status transition.");
            }
            order.setStatus(newStatus);
        }
        
        Order updatedOrder = orderRepository.save(order);
        return orderMapper.toOrderResponse(updatedOrder);
    }

    @Override
    @Transactional
    public void recordVnpayTransaction(Integer orderId, String transactionNo) {
        recordVnpayTransaction(orderId, transactionNo, Collections.emptyMap());
    }

    @Override
    @Transactional
    public void recordVnpayTransaction(Integer orderId, String transactionNo, Map<String, String> vnpParams) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found with id: " + orderId));
                
        if (order.getStatus() == OrderStatus.PENDING) {
            order.setVnpTransactionNo(transactionNo);
            order.setPaymentStatus(PaymentStatus.PAID);
            orderRepository.save(order);

            savePaymentTransaction(order, transactionNo, PaymentStatus.PAID, vnpParams);
        }
    }

    @Override
    @Transactional
    public void recordFailedVnpayTransaction(Integer orderId, Map<String, String> vnpParams) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order != null && order.getPaymentStatus() != PaymentStatus.PAID) {
            savePaymentTransaction(order, null, PaymentStatus.FAILED, vnpParams);
        }
    }

    private void savePaymentTransaction(Order order, String transactionNo, PaymentStatus status, Map<String, String> vnpParams) {
        String txnRef = vnpParams.getOrDefault("vnp_TxnRef", order.getId() + "_" + System.currentTimeMillis());
        BigDecimal amount = BigDecimal.ZERO;
        if (vnpParams.containsKey("vnp_Amount")) {
            try {
                amount = new BigDecimal(vnpParams.get("vnp_Amount")).divide(new BigDecimal(100));
            } catch (Exception ignored) {}
        } else {
            amount = order.getTotalAmount().multiply(EXCHANGE_RATE);
        }

        LocalDateTime payDate = null;
        if (vnpParams.containsKey("vnp_PayDate")) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                payDate = LocalDateTime.parse(vnpParams.get("vnp_PayDate"), formatter);
            } catch (Exception ignored) {}
        }

        PaymentTransaction transaction = PaymentTransaction.builder()
                .order(order)
                .paymentMethod("VNPAY")
                .txnRef(txnRef)
                .transactionNo(transactionNo)
                .amount(amount)
                .bankCode(vnpParams.get("vnp_BankCode"))
                .cardType(vnpParams.get("vnp_CardType"))
                .responseCode(vnpParams.get("vnp_ResponseCode"))
                .status(status)
                .payDate(payDate != null ? payDate : LocalDateTime.now())
                .rawResponse(vnpParams.toString())
                .build();

        paymentTransactionRepository.save(transaction);
    }

    @Override
    public String getPaymentUrl(Integer orderId, HttpServletRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("No order found."));

        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!order.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not authorized to pay for this order.");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("The order is not in the pending payment status.");
        }

        if (!"VNPAY".equalsIgnoreCase(order.getPayment().getMethod())) {
            throw new IllegalStateException("This order was not paid for via VNPay.");
        }
        
        if (order.getVnpTransactionNo() != null || order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new IllegalStateException("This order has already been paid for.");
        }

        String vnp_TxnRef = order.getId() + "_" + System.currentTimeMillis();
        long amount = "USD".equalsIgnoreCase(vnpayCurrency)
                ? order.getTotalAmount().longValue()
                : order.getTotalAmount().multiply(EXCHANGE_RATE).longValue();
        String paymentUrl = vnpayService.createPaymentUrl(
                amount, 
                "Repay the order " + order.getId(), 
                vnp_TxnRef, 
                request
        );

        return paymentUrl;
    }

    @Override
    @Transactional
    public OrderResponse cancelMyOrder(Integer orderId) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found with id: " + orderId));

        if (!order.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("You are not authorized to cancel this order.");
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("You can only cancel an order that is in PENDING status.");
        }

        order.setStatus(OrderStatus.CANCELLED);
        restoreProductStock(order);

        Order cancelledOrder = orderRepository.save(order);
        return orderMapper.toOrderResponse(cancelledOrder);
    }


    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderSummaryResponse> getMyOrders(Integer id, OrderStatus status, Pageable pageable) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Page<Order> orderPage = orderRepository.findOrdersForUser(currentUser, id, status, pageable);
        List<OrderSummaryResponse> responses = orderPage.getContent().stream()
                .map(orderMapper::toOrderSummaryResponse)
                .collect(Collectors.toList());
        return PageResponse.fromPage(orderPage, responses);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminOrderSummaryResponse> getAllOrdersForAdmin(Integer id, String receiverName, OrderStatus status, Pageable pageable) {
        Specification<Order> spec = OrderSpecification.empty();
        if (id != null) spec = spec.and(OrderSpecification.hasId(id));
        if (receiverName != null && !receiverName.isEmpty()) spec = spec.and(OrderSpecification.receiverNameContains(receiverName));
        if (status != null) spec = spec.and(OrderSpecification.hasStatus(status));
        
        Page<Order> orderPage = orderRepository.findAll(spec, pageable);
        List<AdminOrderSummaryResponse> responses = orderPage.getContent().stream()
                .map(orderMapper::toAdminOrderSummaryResponse)
                .collect(Collectors.toList());
        return PageResponse.fromPage(orderPage, responses);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Object getOrderDetail(Integer orderId) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean isAdmin = currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ADMIN"));
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        if (!isAdmin && !order.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("You are not authorized to view this order.");
        }
        
        if (isAdmin) {
            return orderMapper.toAdminOrderDetailResponse(order);
        } else {
            return orderMapper.toOrderDetailResponse(order);
        }
    }

    private void restoreProductStock(Order order) {
        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            stockDeductionContext.restoreStock(stockDeductionContext.getDefaultStrategyType(), order.getOrderItems());
            evictProductCaches(order.getOrderItems());
        }
    }

    private void evictProductCaches(List<OrderItem> orderItems) {
        if (orderItems == null || orderItems.isEmpty()) return;
        try {
            Cache productDetailCache = cacheManager.getCache("product_detail");
            Cache topSellingCache = cacheManager.getCache("products_top_selling");
            if (productDetailCache != null) {
                for (OrderItem item : orderItems) {
                    if (item.getProduct() != null && item.getProduct().getId() != null) {
                        productDetailCache.evict(item.getProduct().getId());
                    }
                }
            }
            if (topSellingCache != null) {
                topSellingCache.clear();
            }
        } catch (Exception ignored) {
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW) 
    public List<Cart> cleanCartOfInvalidItems(User user) {
        List<Cart> originalCartItems = cartRepository.findByUser(user);
        
        if (originalCartItems.isEmpty()) {
            return new ArrayList<>(); 
        }

        List<Integer> productIdsInCart = originalCartItems.stream()
                .filter(cartItem -> cartItem.getProduct() != null)
                .map(cartItem -> cartItem.getProduct().getId())
                .collect(Collectors.toList());

        Map<Integer, Product> productsMap = productRepository.findAllById(productIdsInCart).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        List<Cart> validItems = new ArrayList<>();
        List<Cart> itemsToDelete = new ArrayList<>();

        for (Cart item : originalCartItems) {
            Product originalProduct = item.getProduct();
            if (originalProduct == null) {
                itemsToDelete.add(item);
                continue;
            }
            
            Product product = productsMap.get(originalProduct.getId());
            if (product == null || product.getQuantityInStock() < item.getQuantity()) {
                itemsToDelete.add(item);
            } else {
                validItems.add(item);
            }
        }

        if (!itemsToDelete.isEmpty()) {
            cartRepository.deleteAll(itemsToDelete);
        }
        
        return validItems; 
    }
}