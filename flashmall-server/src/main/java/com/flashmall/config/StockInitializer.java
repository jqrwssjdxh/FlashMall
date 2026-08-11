package com.flashmall.config;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.flashmall.entity.Product;
import com.flashmall.mapper.ProductMapper;
import com.flashmall.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class StockInitializer implements CommandLineRunner {


    private final ProductMapper productMapper;

    private final StockService stockService;


    @Override
    public void run(String... args) {


        System.out.println("开始同步商品库存到Redis");


        productMapper.selectList(
                new LambdaQueryWrapper<>()
        ).forEach(product -> {


            stockService.initStock(
                    product.getId(),
                    product.getStock()
            );


        });


        System.out.println("库存同步完成");


    }
}