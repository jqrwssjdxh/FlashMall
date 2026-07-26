package com.flashmall.service.impl;

import com.flashmall.entity.Product;
import com.flashmall.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Slf4j
public class ProductServiceImpl implements ProductService {

    /** 空值缓存标记（避免缓存穿透） */
    private static final Product NULL_PRODUCT = new Product();

    /** 正常缓存 TTL */
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    /** 空值缓存 TTL（比正常短，防止占用空间） */
    private static final Duration NULL_TTL = Duration.ofMinutes(1);

    private final RedisTemplate<String, Object> redisTemplate;

    public ProductServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Product getProduct(Long id) {
        String key = "product:" + id;

        // ========== 1. 查 Redis ==========
        Product product = (Product) redisTemplate.opsForValue().get(key);

        if (product != null) {
            if (product == NULL_PRODUCT) {
                // 命中的是空值标记 → 直接返回 null，不查 MySQL
                log.info("命中空值缓存，productId={} → 返回 null", id);
                return null;
            }
            log.info("查询Redis缓存，productId={}", id);
            return product;
        }

        // ========== 2. 查 MySQL（当前模拟） ==========
        log.info("查询MySQL，productId={}", id);

        // TODO: 替换为真实 Mapper 查询
        product = queryFromDatabase(id);

        // ========== 3. 写 Redis ==========
        if (product != null) {
            redisTemplate.opsForValue().set(key, product, CACHE_TTL);
            log.info("写入缓存，productId={}，TTL={}", id, CACHE_TTL);
        } else {
            // 空值也缓存，防止缓存穿透
            redisTemplate.opsForValue().set(key, NULL_PRODUCT, NULL_TTL);
            log.info("写入空值缓存，productId={}，TTL={}", id, NULL_TTL);
        }

        return product;
    }

    /** 模拟数据库查询，后续替换为 Mapper */
    private Product queryFromDatabase(Long id) {
        // 假设 id=1 存在，其他不存在（演示空值缓存）
        if (id == 1) {
            Product product = new Product();
            product.setId(id);
            product.setName("iPhone 17");
            product.setStock(100);
            return product;
        }
        return null;
    }
}
