package com.ezbuy.ezbuy.services;

import com.ezbuy.ezbuy.dtos.request.CartRequest;
import com.ezbuy.ezbuy.dtos.response.CartDetailResponse;
import com.ezbuy.ezbuy.dtos.response.CartResponse;
import com.ezbuy.ezbuy.entities.Cart;
import com.ezbuy.ezbuy.entities.Product;
import com.ezbuy.ezbuy.entities.User;
import com.ezbuy.ezbuy.exceptions.NotFoundException;
import com.ezbuy.ezbuy.mappers.CartMapper;
import com.ezbuy.ezbuy.repositories.CartRepository;
import com.ezbuy.ezbuy.repositories.ProductRepository;
import com.ezbuy.ezbuy.services.impl.CartServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CartMapper cartMapper;

    @InjectMocks
    private CartServiceImpl cartService;

    private User sampleUser;
    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1)
                .email("user@example.com")
                .firstName("Van A")
                .lastName("Nguyen")
                .build();

        sampleProduct = Product.builder()
                .id(100)
                .name("iPhone 15 Pro")
                .price(BigDecimal.valueOf(1000))
                .quantityInStock(10)
                .isActive(true)
                .build();

        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        lenient().when(authentication.getPrincipal()).thenReturn(sampleUser);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("addToCart: adds new item when product not yet in cart")
    void addToCart_NewItem_Success() {
        CartRequest request = new CartRequest();
        request.setProductId(100);
        request.setQuantity(2);

        Cart cartItem = Cart.builder().user(sampleUser).product(sampleProduct).quantity(2).build();
        CartResponse cartResponse = CartResponse.builder()
                .productId(100)
                .quantity(2)
                .price(BigDecimal.valueOf(1000))
                .totalPrice(BigDecimal.valueOf(2000))
                .build();

        when(productRepository.findById(100)).thenReturn(Optional.of(sampleProduct));
        when(cartRepository.findByUserAndProduct(sampleUser, sampleProduct)).thenReturn(Optional.empty());
        when(cartRepository.findByUser(sampleUser)).thenReturn(List.of(cartItem));
        when(cartMapper.toCartResponse(cartItem)).thenReturn(cartResponse);

        CartDetailResponse result = cartService.addToCart(request);

        assertThat(result).isNotNull();
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getTotalCartPrice()).isEqualByComparingTo(BigDecimal.valueOf(2000));
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    @DisplayName("addToCart: increments quantity when product already in cart")
    void addToCart_ExistingItem_IncrementsQuantity() {
        CartRequest request = new CartRequest();
        request.setProductId(100);
        request.setQuantity(3);

        Cart existingCart = Cart.builder().user(sampleUser).product(sampleProduct).quantity(2).build();
        CartResponse cartResponse = CartResponse.builder()
                .productId(100)
                .quantity(5)
                .price(BigDecimal.valueOf(1000))
                .totalPrice(BigDecimal.valueOf(5000))
                .build();

        when(productRepository.findById(100)).thenReturn(Optional.of(sampleProduct));
        when(cartRepository.findByUserAndProduct(sampleUser, sampleProduct)).thenReturn(Optional.of(existingCart));
        when(cartRepository.findByUser(sampleUser)).thenReturn(List.of(existingCart));
        when(cartMapper.toCartResponse(existingCart)).thenReturn(cartResponse);

        CartDetailResponse result = cartService.addToCart(request);

        assertThat(result).isNotNull();
        assertThat(existingCart.getQuantity()).isEqualTo(5);
        verify(cartRepository).save(existingCart);
    }

    @Test
    @DisplayName("addToCart: throws exception when requested quantity exceeds stock")
    void addToCart_ExceedsStock_ThrowsException() {
        CartRequest request = new CartRequest();
        request.setProductId(100);
        request.setQuantity(15); // stock is only 10

        when(productRepository.findById(100)).thenReturn(Optional.of(sampleProduct));

        assertThatThrownBy(() -> cartService.addToCart(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Not enough products in stock");

        verify(cartRepository, never()).save(any());
    }

    @Test
    @DisplayName("addToCart: throws NotFoundException when product does not exist")
    void addToCart_ProductNotFound_ThrowsException() {
        CartRequest request = new CartRequest();
        request.setProductId(999);
        request.setQuantity(1);

        when(productRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addToCart(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Product not found");
    }

    @Test
    @DisplayName("updateCart: updates quantity successfully")
    void updateCart_Success() {
        CartRequest request = new CartRequest();
        request.setProductId(100);
        request.setQuantity(4);

        Cart cartItem = Cart.builder().user(sampleUser).product(sampleProduct).quantity(2).build();
        CartResponse cartResponse = CartResponse.builder()
                .productId(100)
                .quantity(4)
                .totalPrice(BigDecimal.valueOf(4000))
                .build();

        when(productRepository.findById(100)).thenReturn(Optional.of(sampleProduct));
        when(cartRepository.findByUserAndProduct(sampleUser, sampleProduct)).thenReturn(Optional.of(cartItem));
        when(cartRepository.findByUser(sampleUser)).thenReturn(List.of(cartItem));
        when(cartMapper.toCartResponse(cartItem)).thenReturn(cartResponse);

        CartDetailResponse result = cartService.updateCart(request);

        assertThat(result).isNotNull();
        assertThat(cartItem.getQuantity()).isEqualTo(4);
        verify(cartRepository).save(cartItem);
    }

    @Test
    @DisplayName("removeFromCart: removes item from cart")
    void removeFromCart_Success() {
        Cart cartItem = Cart.builder().user(sampleUser).product(sampleProduct).quantity(2).build();

        when(productRepository.findById(100)).thenReturn(Optional.of(sampleProduct));
        when(cartRepository.findByUserAndProduct(sampleUser, sampleProduct)).thenReturn(Optional.of(cartItem));
        when(cartRepository.findByUser(sampleUser)).thenReturn(Collections.emptyList());

        CartDetailResponse result = cartService.removeFromCart(100);

        assertThat(result).isNotNull();
        assertThat(result.getItems()).isEmpty();
        verify(cartRepository).delete(cartItem);
    }

    @Test
    @DisplayName("clearCart: deletes all items of user")
    void clearCart_Success() {
        when(cartRepository.findByUser(sampleUser)).thenReturn(Collections.emptyList());

        CartDetailResponse result = cartService.clearCart();

        assertThat(result).isNotNull();
        assertThat(result.getItems()).isEmpty();
        verify(cartRepository).deleteByUser(sampleUser);
    }
}
