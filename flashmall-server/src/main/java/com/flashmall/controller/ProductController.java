package com.flashmall.controller;

import com.flashmall.common.Result;
import com.flashmall.dto.ProductCreateDTO;
import com.flashmall.dto.ProductQueryDTO;
import com.flashmall.dto.ProductStatusDTO;
import com.flashmall.dto.ProductUpdateDTO;
import com.flashmall.entity.Product;
import com.flashmall.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/{id}")
    public Result<Product> getProduct(@PathVariable Long id) {
        return Result.success(productService.getProduct(id));
    }

    @GetMapping("/list")
    public Result<Page<Product>> list(ProductQueryDTO queryDTO) {
        return Result.success(productService.list(queryDTO));
    }

    @PostMapping
    public Result<Void> add(
            @Valid @RequestBody ProductCreateDTO dto
    ){

        productService.add(dto);

        return Result.success();
    }
    @PutMapping("/{id}")
    public Result<Void> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateDTO dto
    ){

        productService.update(id, dto);

        return Result.success();
    }
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody ProductStatusDTO dto
    ){

        productService.updateStatus(id, dto);

        return Result.success();
    }
}
