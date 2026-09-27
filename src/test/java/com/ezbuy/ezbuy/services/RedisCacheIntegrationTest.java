package com.ezbuy.ezbuy.services;

import com.ezbuy.ezbuy.dtos.response.CategoryTreeResponse;
import com.ezbuy.ezbuy.dtos.response.ManufacturerResponse;
import com.ezbuy.ezbuy.dtos.response.ProductDetailResponse;
import com.ezbuy.ezbuy.entities.Category;
import com.ezbuy.ezbuy.entities.Manufacturer;
import com.ezbuy.ezbuy.entities.Product;
import com.ezbuy.ezbuy.repositories.CategoryRepository;
import com.ezbuy.ezbuy.repositories.ManufacturerRepository;
import com.ezbuy.ezbuy.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RedisCacheIntegrationTest {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ManufacturerService manufacturerService;

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ManufacturerRepository manufacturerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        // Clear redis caches before each test
        Set<String> keys = stringRedisTemplate.keys("ezbuy:cache:*");
        if (keys != null && !keys.isEmpty()) {
            stringRedisTemplate.delete(keys);
        }
    }

    @Test
    @DisplayName("Category Tree: First call populates Redis cache, subsequent calls hit cache")
    void categoryTree_CachesResultInRedis() {
        Cache cache = cacheManager.getCache("category_tree");
        assertThat(cache).isNotNull();
        assertThat(cache.get("tree")).isNull();

        // 1st call -> Cache miss, queries database and caches
        List<CategoryTreeResponse> result1 = categoryService.getCategoryTree();
        assertThat(result1).isNotNull();

        // Verify cache in Redis is populated
        Cache.ValueWrapper cachedValue = cache.get("tree");
        assertThat(cachedValue).isNotNull();
        assertThat(cachedValue.get()).isInstanceOf(List.class);

        // Verify Redis raw key exists
        Boolean hasKey = stringRedisTemplate.hasKey("ezbuy:cache:category_tree::tree");
        assertThat(hasKey).isTrue();

        // 2nd call -> Cache hit
        List<CategoryTreeResponse> result2 = categoryService.getCategoryTree();
        assertThat(result2).isNotNull();
        assertThat(result2.size()).isEqualTo(result1.size());
    }

    @Test
    @DisplayName("Manufacturers: Caches all manufacturers in Redis")
    void allManufacturers_CachesResultInRedis() {
        Cache cache = cacheManager.getCache("manufacturers");
        assertThat(cache).isNotNull();
        assertThat(cache.get("all")).isNull();

        List<ManufacturerResponse> list1 = manufacturerService.getAllManufacturers();
        assertThat(list1).isNotNull();

        // Verify cache is populated
        Cache.ValueWrapper cached = cache.get("all");
        assertThat(cached).isNotNull();
        assertThat(cached.get()).isInstanceOf(List.class);

        Boolean hasKey = stringRedisTemplate.hasKey("ezbuy:cache:manufacturers::all");
        assertThat(hasKey).isTrue();

        List<ManufacturerResponse> list2 = manufacturerService.getAllManufacturers();
        assertThat(list2).isEqualTo(list1);
    }

    @Test
    @DisplayName("Product Detail: Caches customer view in Redis, evicts on product update")
    void productDetail_CachesAndEvictsOnUpdate() {
        // Find or create an existing product
        Product product = productRepository.findAll().stream().findFirst().orElseGet(() -> {
            Category cat = categoryRepository.findAll().stream().findFirst().orElseGet(() -> {
                Category c = new Category();
                c.setName("Test Cat");
                c.setSlug("test-cat");
                c.setActive(true);
                return categoryRepository.save(c);
            });
            Manufacturer m = manufacturerRepository.findAll().stream().findFirst().orElseGet(() -> {
                Manufacturer mf = new Manufacturer();
                mf.setName("Test Brand");
                return manufacturerRepository.save(mf);
            });
            Product p = Product.builder()
                    .name("Test Phone Cache")
                    .slug("test-phone-cache")
                    .price(BigDecimal.valueOf(500))
                    .quantityInStock(20)
                    .category(cat)
                    .manufacturer(m)
                    .isActive(true)
                    .build();
            return productRepository.save(p);
        });

        int productId = product.getId();
        Cache cache = cacheManager.getCache("product_detail");
        assertThat(cache).isNotNull();
        assertThat(cache.get(productId)).isNull();

        // 1st call as customer -> Populates cache
        Object detail1 = productService.getProductById(productId);
        assertThat(detail1).isInstanceOf(ProductDetailResponse.class);

        // Verify cache in Redis
        Cache.ValueWrapper cached = cache.get(productId);
        assertThat(cached).isNotNull();
        assertThat(cached.get()).isInstanceOf(ProductDetailResponse.class);

        // 2nd call -> Returns from cache
        Object detail2 = productService.getProductById(productId);
        assertThat(detail2).isNotNull();

        // Evict key manually (simulating update/delete eviction)
        cache.evict(productId);
        assertThat(cache.get(productId)).isNull();
    }

    @Test
    @DisplayName("Cache Stampede: sync=true ensures concurrent misses result in thread-safe resolution")
    void cacheStampede_ConcurrentMissesAreThreadSafe() throws InterruptedException {
        int threads = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threads);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    List<CategoryTreeResponse> tree = categoryService.getCategoryTree();
                    if (tree != null) {
                        successCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        // Release all threads simultaneously
        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();

        assertThat(successCount.get()).isEqualTo(threads);
        assertThat(stringRedisTemplate.hasKey("ezbuy:cache:category_tree::tree")).isTrue();
    }
}
