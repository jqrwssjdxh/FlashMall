package com.flashmall.service.impl;

import com.flashmall.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;


@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String STOCK_KEY_PREFIX = "product:stock:";

    private final RedisScript<Long> decreaseStockScript;

    @Override
    public boolean decreaseStock(Long productId, Integer count) {
        String key = STOCK_KEY_PREFIX + productId;

        Long result = stringRedisTemplate.execute(
                decreaseStockScript,
                Collections.singletonList(key),
                String.valueOf(count)
        );

        return result != null && result == 1L;
    }

    @Override
    public void initStock(Long productId, Integer stock) {
        String key = STOCK_KEY_PREFIX + productId;
        stringRedisTemplate.opsForValue().set(key, String.valueOf(stock));
    }

    @Override
    public void restoreStock(Long productId, Integer count) {


        String key =
                STOCK_KEY_PREFIX + productId;


        stringRedisTemplate
                .opsForValue()
                .increment(
                        key,
                        count
                );

    }
}
