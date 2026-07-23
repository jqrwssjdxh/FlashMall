package com.flashmall.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    /**
     * JWT秘钥
     */
    private String secret;
    /**
     * 过期时间(毫秒)
     */
    private Long expiration;
}
