package com.ezbuy.ezbuy.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.EVERYTHING,
                JsonTypeInfo.As.WRAPPER_ARRAY
        );
        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory,
                                                       GenericJackson2JsonRedisSerializer jsonSerializer) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory,
                                         GenericJackson2JsonRedisSerializer jsonSerializer) {
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer))
                .prefixCacheNameWith("ezbuy:cache:")
                .disableCachingNullValues()
                .entryTtl(Duration.ofMinutes(30));

        Map<String, RedisCacheConfiguration> initialConfigs = new HashMap<>();

        // Category tree: 60 minutes + random jitter (±5m)
        initialConfigs.put("category_tree", defaultCacheConfig.entryTtl(ttlWithJitter(Duration.ofMinutes(60), 300)));
        // Categories list: 60 minutes + random jitter (±5m)
        initialConfigs.put("categories", defaultCacheConfig.entryTtl(ttlWithJitter(Duration.ofMinutes(60), 300)));
        // Manufacturers list: 60 minutes + random jitter (±5m)
        initialConfigs.put("manufacturers", defaultCacheConfig.entryTtl(ttlWithJitter(Duration.ofMinutes(60), 300)));
        // Product detail: 30 minutes + random jitter (±3m)
        initialConfigs.put("product_detail", defaultCacheConfig.entryTtl(ttlWithJitter(Duration.ofMinutes(30), 180)));
        // Products top selling: 15 minutes + random jitter (±2m)
        initialConfigs.put("products_top_selling", defaultCacheConfig.entryTtl(ttlWithJitter(Duration.ofMinutes(15), 120)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultCacheConfig)
                .withInitialCacheConfigurations(initialConfigs)
                .build();
    }

    /**
     * Helper to create dynamic TTL with random jitter to prevent Cache Avalanche.
     */
    private Duration ttlWithJitter(Duration baseTtl, long maxJitterSeconds) {
        long jitter = ThreadLocalRandom.current().nextLong(-maxJitterSeconds, maxJitterSeconds + 1);
        long totalSeconds = Math.max(60, baseTtl.toSeconds() + jitter);
        return Duration.ofSeconds(totalSeconds);
    }
}

