package com.ezbuy.ezbuy.controllers;

import com.ezbuy.ezbuy.dtos.response.PageResponse;
import com.ezbuy.ezbuy.dtos.response.ProductDetailResponse;
import com.ezbuy.ezbuy.dtos.response.ProductResponse;
import com.ezbuy.ezbuy.exceptions.GlobalExceptionHandler;
import com.ezbuy.ezbuy.exceptions.NotFoundException;
import com.ezbuy.ezbuy.services.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/products: returns 200 OK with paged product list")
    void getAllProducts_Success() throws Exception {
        ProductResponse product = ProductResponse.builder()
                .id(1)
                .name("iPhone 15")
                .price(BigDecimal.valueOf(1000))
                .build();

        PageResponse<ProductResponse> pageResponse = PageResponse.<ProductResponse>builder()
                .content(List.of(product))
                .pageNumber(0)
                .pageSize(12)
                .totalElements(1)
                .totalPages(1)
                .last(true)
                .build();

        when(productService.getAllProducts(eq(null), eq(null), eq(null), any(Pageable.class)))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/products")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].name").value("iPhone 15"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /api/products/{id}: returns 200 OK with product details")
    void getProductById_Success() throws Exception {
        ProductDetailResponse detail = ProductDetailResponse.builder()
                .id(10)
                .name("Samsung Galaxy S24")
                .price(BigDecimal.valueOf(900))
                .build();

        when(productService.getProductById(10)).thenReturn(detail);

        mockMvc.perform(get("/api/products/10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.name").value("Samsung Galaxy S24"));
    }

    @Test
    @DisplayName("GET /api/products/{id}: returns 404 Not Found when product does not exist")
    void getProductById_NotFound() throws Exception {
        when(productService.getProductById(999))
                .thenThrow(new NotFoundException("Product not found with id: 999"));

        mockMvc.perform(get("/api/products/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Product not found with id: 999"));
    }
}
