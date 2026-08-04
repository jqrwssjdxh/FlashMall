package com.flashmall.service;


import com.flashmall.dto.CartAddDTO;
import com.flashmall.vo.CartVO;

import java.util.List;


public interface CartService {


    /**
     * 添加购物车
     */
    void add(CartAddDTO dto);


    /**
     * 查询当前用户购物车
     */
    List<CartVO> getMyCart();


    /**
     * 修改购物车数量
     */
    void update(Long id, Integer quantity);


    /**
     * 删除购物车商品
     */
    void delete(Long id);

}