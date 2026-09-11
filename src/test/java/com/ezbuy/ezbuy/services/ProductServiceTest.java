package com.ezbuy.ezbuy.services;

import com.ezbuy.ezbuy.dtos.request.ProductRequest;
import com.ezbuy.ezbuy.dtos.response.PageResponse;
import com.ezbuy.ezbuy.dtos.response.ProductDetailResponse;
import com.ezbuy.ezbuy.dtos.response.ProductResponse;
import com.ezbuy.ezbuy.entities.Category;
import com.ezbuy.ezbuy.entities.Manufacturer;
import com.ezbuy.ezbuy.entities.Product;
import com.ezbuy.ezbuy.events.ProductDeactivatedEvent;
import com.ezbuy.ezbuy.exceptions.NotFoundException;
import com.ezbuy.ezbuy.mappers.ProductMapper;
import com.ezbuy.ezbuy.repositories.CategoryRepository;
import com.ezbuy.ezbuy.repositories.ManufacturerRepository;
import com.ezbuy.ezbuy.repositories.ProductRepository;
import com.ezbuy.ezbuy.services.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ManufacturerRepository manufacturerRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private CategoryService categoryService;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private CloudinaryService cloudinaryService;

    @InjectMocks
    private ProductServiceImpl productService;

    private Category sampleCategory;
    private Manufacturer sampleManufacturer;
    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        sampleCategory = Category.builder()
                .id(1)
                .name("Smartphones")
                .build();

        sampleManufacturer = Manufacturer.builder()
                .id(1)
                .name("Apple")
                .build();

        sampleProduct = Product.builder()
                .id(10)
                .name("iPhone 15 Pro")
                .slug("iphone-15-pro")
                .price(BigDecimal.valueOf(999))
                .quantityInStock(50)
                .category(sampleCategory)
                .manufacturer(sampleManufacturer)
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("createProduct: creates product with image upload and slug generation")
    void createProduct_Success() {
        ProductRequest request = new ProductRequest();
        request.setName("iPhone 15 Pro");
        request.setCategoryId(1);
        request.setManufacturerId(1);
        request.setPrice(BigDecimal.valueOf(999));
        request.setQuantityInStock(50);

        MockMultipartFile file = new MockMultipartFile("file", "image.png", "image/png", "bytes".getBytes());

        Product entityToSave = Product.builder().name("iPhone 15 Pro").build();
        ProductResponse response = ProductResponse.builder().id(10).name("iPhone 15 Pro").build();

        when(categoryRepository.findById(1)).thenReturn(Optional.of(sampleCategory));
        when(manufacturerRepository.findById(1)).thenReturn(Optional.of(sampleManufacturer));
        when(productMapper.toProduct(request)).thenReturn(entityToSave);
        when(productRepository.existsBySlug("iphone-15-pro")).thenReturn(false);
        when(cloudinaryService.uploadFile(file, "products")).thenReturn("https://cloudinary.com/image.png");
        when(productRepository.save(entityToSave)).thenReturn(sampleProduct);
        when(productMapper.toProductResponse(sampleProduct)).thenReturn(response);

        ProductResponse result = productService.createProduct(request, file);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("iPhone 15 Pro");
        verify(cloudinaryService).uploadFile(file, "products");
        verify(productRepository).save(entityToSave);
    }

    @Test
    @DisplayName("createProduct: throws NotFoundException when category does not exist")
    void createProduct_CategoryNotFound_ThrowsException() {
        ProductRequest request = new ProductRequest();
        request.setCategoryId(999);

        when(categoryRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.createProduct(request, null))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Category not found");

        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("getProductById: returns product detail when found")
    void getProductById_Success() {
        ProductDetailResponse detailResponse = ProductDetailResponse.builder()
                .id(10)
                .name("iPhone 15 Pro")
                .build();

        when(productRepository.findById(10)).thenReturn(Optional.of(sampleProduct));
        when(productMapper.toProductDetailResponse(sampleProduct)).thenReturn(detailResponse);

        Object result = productService.getProductById(10);

        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(ProductDetailResponse.class);
    }

    @Test
    @DisplayName("getProductById: throws NotFoundException when id not found")
    void getProductById_NotFound_ThrowsException() {
        when(productRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(999))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Product not found with id: 999");
    }

    @Test
    @DisplayName("getAllProducts: returns paged product list")
    void getAllProducts_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(List.of(sampleProduct), pageable, 1);
        ProductResponse productResponse = ProductResponse.builder().id(10).name("iPhone 15 Pro").build();

        when(productRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(productPage);
        when(productMapper.toProductResponse(sampleProduct)).thenReturn(productResponse);

        PageResponse<ProductResponse> result = productService.getAllProducts(null, null, null, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("deleteProduct: sets isActive=false and publishes ProductDeactivatedEvent")
    void deleteProduct_Success() {
        when(productRepository.findById(10)).thenReturn(Optional.of(sampleProduct));

        productService.deleteProduct(10);

        assertThat(sampleProduct.isActive()).isFalse();
        assertThat(sampleProduct.getDeletedAt()).isNotNull();
        verify(productRepository).save(sampleProduct);
        verify(eventPublisher).publishEvent(any(ProductDeactivatedEvent.class));
    }
}
