package com.flashmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.flashmall.constant.RedisKeyConstant;
import com.flashmall.dto.ProductCreateDTO;
import com.flashmall.dto.ProductQueryDTO;
import com.flashmall.dto.ProductStatusDTO;
import com.flashmall.dto.ProductUpdateDTO;
import com.flashmall.entity.Product;
import com.flashmall.mapper.ProductMapper;
import com.flashmall.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j

public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    // ==================== 缓存相关常量 ====================
    private static final long CACHE_TTL_MINUTES = 30;

    // ==================== 主流程 ====================

    @Override
    public Product getProduct(Long id) {
        String key = RedisKeyConstant.PRODUCT_CACHE_KEY + id;

        // 1. 查缓存
        Product product = getProductFromCache(key);
        if (product != null) {
            log.info("商品缓存命中，id={}", id);
            return product;
        }

        // 2. 查数据库
        log.info("商品缓存未命中，查询数据库，id={}", id);
        product = getProductFromDatabase(id);

        // 3. 回填缓存（无论是否有值，都占位）
        if (product != null) {
            saveToCache(key, product);
        } else {
            // 可选：空值缓存，防止穿透（如果你需要的话）
            // saveNullToCache(key);
            log.info("商品不存在，id={}，不写入缓存", id);
        }

        return product;
    }

    @Override
    public Page<Product> list(ProductQueryDTO queryDTO) {

        Page<Product> page =
                new Page<>(queryDTO.getPage(), queryDTO.getSize());

        LambdaQueryWrapper<Product> wrapper =
                new LambdaQueryWrapper<>();

        wrapper.like(
                StringUtils.hasText(queryDTO.getName()),
                Product::getName,
                queryDTO.getName()
        );

        wrapper.eq(
                queryDTO.getStatus() != null,
                Product::getStatus,
                queryDTO.getStatus()
        );

        return productMapper.selectPage(page, wrapper);
    }

    @Override
    public void add(ProductCreateDTO dto){

        Product product = new Product();

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setStatus(dto.getStatus());

        productMapper.insert(product);

    }

    @Override
    public void update(Long id, ProductUpdateDTO dto) {

        Product product = new Product();

        product.setId(id);
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setStatus(dto.getStatus());

        productMapper.updateById(product);


        String key = RedisKeyConstant.PRODUCT_CACHE_KEY + id;


        redisTemplate.delete(key);
    }

    // ==================== 私有方法：各司其职 ====================

    /**
     * 从 Redis 缓存中获取商品
     */
    private Product getProductFromCache(String key) {
        Object cache = redisTemplate.opsForValue().get(key);
        if (cache instanceof Product product) {
            return product;
        }
        return null;
    }

    /**
     * 从数据库获取商品
     */
    private Product getProductFromDatabase(Long id) {
        return productMapper.selectById(id);
    }

    /**
     * 写入 Redis 缓存
     */
    private void saveToCache(String key, Product product) {
        redisTemplate.opsForValue().set(key, product, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        log.info("商品写入缓存，id={}，TTL={}分钟", product.getId(), CACHE_TTL_MINUTES);
    }

    /**
     * （可选）空值缓存，防止缓存穿透
     */
    private void saveNullToCache(String key) {
        // 空值缓存，TTL 设短一点，比如 1 分钟
        redisTemplate.opsForValue().set(key, new Product(), 1, TimeUnit.MINUTES);
        log.info("空值写入缓存，key={}，TTL=1分钟", key);
    }

    @Override
    public void updateStatus(Long id, ProductStatusDTO dto) {

        Product product = new Product();

        product.setId(id);
        product.setStatus(dto.getStatus());

        productMapper.updateById(product);

        String key = RedisKeyConstant.PRODUCT_CACHE_KEY + id;

        redisTemplate.delete(key);
    }
    
}