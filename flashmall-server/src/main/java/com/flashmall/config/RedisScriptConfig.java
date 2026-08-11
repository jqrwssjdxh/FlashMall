package com.flashmall.config;


import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RedisScriptConfig {


    @Bean
    public DefaultRedisScript<Long> decreaseStockScript(){


        DefaultRedisScript<Long> script =
                new DefaultRedisScript<>();


        script.setLocation(
                new ClassPathResource(
                        "lua/decrease_stock.lua"
                )
        );


        script.setResultType(Long.class);


        return script;

    }

}
