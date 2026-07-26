package com.flashmall.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {


    @Bean
    public RedisTemplate<String,Object> redisTemplate(
            RedisConnectionFactory factory
    ){

        RedisTemplate<String,Object> template =
                new RedisTemplate<>();

        template.setConnectionFactory(factory);


        StringRedisSerializer stringSerializer =
                new StringRedisSerializer();


        GenericJackson2JsonRedisSerializer jsonSerializer =
                new GenericJackson2JsonRedisSerializer();


        // key
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);


        // value
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);


        // 默认序列化
        template.setDefaultSerializer(jsonSerializer);


        template.afterPropertiesSet();

        return template;
    }
}