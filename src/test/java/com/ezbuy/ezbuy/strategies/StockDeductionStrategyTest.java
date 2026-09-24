package com.ezbuy.ezbuy.strategies;

import com.ezbuy.ezbuy.entities.OrderItem;
import com.ezbuy.ezbuy.entities.Product;
import com.ezbuy.ezbuy.enums.StockStrategyType;
import com.ezbuy.ezbuy.exceptions.InsufficientStockException;
import com.ezbuy.ezbuy.repositories.ProductRepository;
import com.ezbuy.ezbuy.strategies.stock.StockDeductionContext;
import com.ezbuy.ezbuy.strategies.stock.StockDeductionStrategy;
import com.ezbuy.ezbuy.strategies.stock.impl.AtomicSqlStockStrategy;
import com.ezbuy.ezbuy.strategies.stock.impl.NaiveStockStrategy;
import com.ezbuy.ezbuy.strategies.stock.impl.PessimisticStockStrategy;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockDeductionStrategyTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private HttpServletRequest httpServletRequest;

    private NaiveStockStrategy naiveStrategy;
    private PessimisticStockStrategy pessimisticStrategy;
    private AtomicSqlStockStrategy atomicSqlStrategy;
    private StockDeductionContext stockDeductionContext;

    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        naiveStrategy = new NaiveStockStrategy(productRepository);
        pessimisticStrategy = new PessimisticStockStrategy(productRepository);
        atomicSqlStrategy = new AtomicSqlStockStrategy(productRepository);

        List<StockDeductionStrategy> strategies = List.of(naiveStrategy, pessimisticStrategy, atomicSqlStrategy);
        stockDeductionContext = new StockDeductionContext(strategies);
        ReflectionTestUtils.setField(stockDeductionContext, "defaultStrategyName", "ATOMIC_SQL");
        stockDeductionContext.init();

        sampleProduct = Product.builder()
                .id(1)
                .name("Test Phone")
                .price(BigDecimal.valueOf(500))
                .quantityInStock(10)
                .isActive(true)
                .build();
    }

    // ==========================================
    // Context Resolution Tests
    // ==========================================
    @Test
    @DisplayName("StockDeductionContext: resolves strategy from HTTP Header")
    void context_ResolvesFromHeader() {
        when(httpServletRequest.getHeader(StockDeductionContext.HEADER_STOCK_STRATEGY)).thenReturn("PESSIMISTIC");

        StockStrategyType resolved = stockDeductionContext.resolveStrategyType(httpServletRequest);
        assertThat(resolved).isEqualTo(StockStrategyType.PESSIMISTIC);

        StockDeductionStrategy strategy = stockDeductionContext.getStrategy(resolved);
        assertThat(strategy).isInstanceOf(PessimisticStockStrategy.class);
    }

    @Test
    @DisplayName("StockDeductionContext: resolves strategy from query param when header missing")
    void context_ResolvesFromParam() {
        when(httpServletRequest.getHeader(StockDeductionContext.HEADER_STOCK_STRATEGY)).thenReturn(null);
        when(httpServletRequest.getParameter(StockDeductionContext.PARAM_STOCK_STRATEGY)).thenReturn("NAIVE");

        StockStrategyType resolved = stockDeductionContext.resolveStrategyType(httpServletRequest);
        assertThat(resolved).isEqualTo(StockStrategyType.NAIVE);

        StockDeductionStrategy strategy = stockDeductionContext.getStrategy(resolved);
        assertThat(strategy).isInstanceOf(NaiveStockStrategy.class);
    }

    @Test
    @DisplayName("StockDeductionContext: falls back to default config when header and param missing")
    void context_FallsBackToDefault() {
        when(httpServletRequest.getHeader(StockDeductionContext.HEADER_STOCK_STRATEGY)).thenReturn(null);
        when(httpServletRequest.getParameter(StockDeductionContext.PARAM_STOCK_STRATEGY)).thenReturn(null);

        StockStrategyType resolved = stockDeductionContext.resolveStrategyType(httpServletRequest);
        assertThat(resolved).isEqualTo(StockStrategyType.ATOMIC_SQL);
    }

    // ==========================================
    // AtomicSqlStockStrategy Tests
    // ==========================================
    @Test
    @DisplayName("AtomicSqlStockStrategy: deduct succeeds when row updated")
    void atomicSql_Deduct_Success() {
        when(productRepository.deductStockAtomic(1, 3)).thenReturn(1);
        sampleProduct.setQuantityInStock(7);
        when(productRepository.findById(1)).thenReturn(Optional.of(sampleProduct));

        int remaining = atomicSqlStrategy.deduct(1, 3);

        assertThat(remaining).isEqualTo(7);
        verify(productRepository).deductStockAtomic(1, 3);
    }

    @Test
    @DisplayName("AtomicSqlStockStrategy: deduct throws InsufficientStockException when 0 rows updated")
    void atomicSql_Deduct_InsufficientStock() {
        when(productRepository.deductStockAtomic(1, 15)).thenReturn(0);
        when(productRepository.findById(1)).thenReturn(Optional.of(sampleProduct));

        assertThatThrownBy(() -> atomicSqlStrategy.deduct(1, 15))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Insufficient stock for product 'Test Phone'");
    }

    // ==========================================
    // PessimisticStockStrategy Tests
    // ==========================================
    @Test
    @DisplayName("PessimisticStockStrategy: deduct succeeds using findAndLockById")
    void pessimistic_Deduct_Success() {
        when(productRepository.findAndLockById(1)).thenReturn(Optional.of(sampleProduct));

        int remaining = pessimisticStrategy.deduct(1, 4);

        assertThat(remaining).isEqualTo(6);
        assertThat(sampleProduct.getQuantityInStock()).isEqualTo(6);
        verify(productRepository).save(sampleProduct);
    }

    @Test
    @DisplayName("PessimisticStockStrategy: deduct throws InsufficientStockException when stock < quantity")
    void pessimistic_Deduct_InsufficientStock() {
        when(productRepository.findAndLockById(1)).thenReturn(Optional.of(sampleProduct));

        assertThatThrownBy(() -> pessimisticStrategy.deduct(1, 20))
                .isInstanceOf(InsufficientStockException.class);

        verify(productRepository, never()).save(any());
    }

    // ==========================================
    // NaiveStockStrategy Tests
    // ==========================================
    @Test
    @DisplayName("NaiveStockStrategy: deduct succeeds")
    void naive_Deduct_Success() {
        when(productRepository.findById(1)).thenReturn(Optional.of(sampleProduct));

        int remaining = naiveStrategy.deduct(1, 2);

        assertThat(remaining).isEqualTo(8);
        verify(productRepository).save(sampleProduct);
    }

    @Test
    @DisplayName("NaiveStockStrategy: deduct throws InsufficientStockException when initial stock is low")
    void naive_Deduct_InsufficientStock() {
        when(productRepository.findById(1)).thenReturn(Optional.of(sampleProduct));

        assertThatThrownBy(() -> naiveStrategy.deduct(1, 50))
                .isInstanceOf(InsufficientStockException.class);
    }
}
