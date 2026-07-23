package com.flashmall.utils;

import com.flashmall.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private final JwtProperties jwtProperties;

    public JwtUtil(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }


    /**
     * 生成Token
     */
    public String generateToken(Long userId, String username) {


        SecretKey key = Keys.hmacShaKeyFor(
                jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)//加密算法（HMAC-SHA）底层处理数据时，认的是 0 和 1 的字节，不认人类的文字字符
        );

/**
 *链式调用
 */
        return Jwts.builder()
                .subject(String.valueOf(userId))//JWT 标准里的“主体”字段，通常用来存唯一标识（比如用户主键）。
                .claim("username", username)
                .issuedAt(new Date())
                .expiration(
                        new Date(
                                System.currentTimeMillis()
                                        + jwtProperties.getExpiration()
                        )
                )
                .signWith(key)
                .compact();//。这个字符串，就是最终交给前端的 Token！


    }


    /**
     * 解析Token
     */
    public Claims parseToken(String token) {

/**
 * 在加密世界里，只有用同一把钥匙（同一个 secret），才能解开用这把钥匙盖的钢印
 */
        SecretKey key = Keys.hmacShaKeyFor(
                jwtProperties.getSecret().getBytes()
        );


        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)//主要验证工作 签名验证+过期验证
                .getPayload();
    }
}


