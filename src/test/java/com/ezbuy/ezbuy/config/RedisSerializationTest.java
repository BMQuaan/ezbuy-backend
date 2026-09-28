package com.ezbuy.ezbuy.config;

import com.ezbuy.ezbuy.dtos.response.CategoryTreeResponse;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RedisSerializationTest {

    @Test
    void testListSerialization() {
        List<CategoryTreeResponse> list = new ArrayList<>();
        list.add(CategoryTreeResponse.builder().id(1).name("Electronics").children(new ArrayList<>()).build());

        // Test with custom configured serializer
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.EVERYTHING,
                JsonTypeInfo.As.WRAPPER_ARRAY
        );

        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(mapper);
        byte[] bytes = serializer.serialize(list);
        System.out.println("JSON: " + new String(bytes));

        Object deserialized = serializer.deserialize(bytes);
        assertThat(deserialized).isInstanceOf(List.class);
        List<?> res = (List<?>) deserialized;
        assertThat(res).hasSize(1);
        assertThat(res.get(0)).isInstanceOf(CategoryTreeResponse.class);
    }

    @Test
    void testSingleObjectSerialization() {
        com.ezbuy.ezbuy.dtos.response.ProductDetailResponse product = com.ezbuy.ezbuy.dtos.response.ProductDetailResponse.builder()
                .id(10)
                .name("MacBook Pro")
                .price(java.math.BigDecimal.valueOf(1999.99))
                .quantityInStock(15)
                .categoryName("Laptops")
                .build();

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.EVERYTHING,
                JsonTypeInfo.As.WRAPPER_ARRAY
        );

        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(mapper);
        byte[] bytes = serializer.serialize(product);
        System.out.println("Product JSON: " + new String(bytes));

        Object deserialized = serializer.deserialize(bytes);
        assertThat(deserialized).isInstanceOf(com.ezbuy.ezbuy.dtos.response.ProductDetailResponse.class);
        com.ezbuy.ezbuy.dtos.response.ProductDetailResponse p = (com.ezbuy.ezbuy.dtos.response.ProductDetailResponse) deserialized;
        assertThat(p.getId()).isEqualTo(10);
        assertThat(p.getName()).isEqualTo("MacBook Pro");
    }
}
