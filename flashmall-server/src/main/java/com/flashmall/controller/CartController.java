package com.flashmall.controller;


import com.flashmall.common.Result;
import com.flashmall.dto.CartAddDTO;
import com.flashmall.service.CartService;
import com.flashmall.vo.CartVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {


    private final CartService cartService;


    /**
     * 添加购物车
     */
    @PostMapping("/add")
    public Result<Void> add(
            @Valid @RequestBody CartAddDTO dto
    ){

        cartService.add(dto);

        return Result.success();

    }



    /**
     * 查看我的购物车
     */
    @GetMapping
    public Result<List<CartVO>> getMyCart(){

        return Result.success(
                cartService.getMyCart()
        );

    }



    /**
     * 修改数量
     */
    @PutMapping("/{id}")
    public Result<Void> update(
            @PathVariable Long id,
            @RequestParam Integer quantity
    ){

        cartService.update(id,quantity);

        return Result.success();

    }



    /**
     * 删除商品
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @PathVariable Long id
    ){

        cartService.delete(id);

        return Result.success();

    }

}