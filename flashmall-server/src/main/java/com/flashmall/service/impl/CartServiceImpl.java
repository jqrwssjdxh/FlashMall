package com.flashmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.flashmall.common.UserContext;
import com.flashmall.constant.ResultCode;
import com.flashmall.dto.CartAddDTO;
import com.flashmall.entity.Cart;
import com.flashmall.entity.Product;
import com.flashmall.exception.BusinessException;
import com.flashmall.mapper.CartMapper;
import com.flashmall.mapper.ProductMapper;
import com.flashmall.service.CartService;
import com.flashmall.vo.CartVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Service
public class CartServiceImpl
        extends ServiceImpl<CartMapper, Cart>
        implements CartService {

    private final ProductMapper productMapper;

    @Override
    public void add(CartAddDTO dto) {
        Long userId = UserContext.getUserId();

        Product product = productMapper.selectById(dto.getProductId());
        if (product == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }

        // 查询购物车是否已有
        Cart cart = this.getOne(
                new LambdaQueryWrapper<Cart>()
                        .eq(Cart::getUserId, userId)
                        .eq(Cart::getProductId, dto.getProductId())
        );

        int newQty = cart != null
                ? cart.getQuantity() + dto.getQuantity()
                : dto.getQuantity();

        if (newQty > product.getStock()) {
            throw new BusinessException(ResultCode.OUT_OF_STOCK);
        }

        if (cart != null) {
            cart.setQuantity(newQty);
            this.updateById(cart);
        } else {
            Cart newCart = new Cart();
            newCart.setUserId(userId);
            newCart.setProductId(product.getId());
            newCart.setProductName(product.getName());
            newCart.setPrice(product.getPrice());
            newCart.setQuantity(dto.getQuantity());
            this.save(newCart);
        }
    }

    @Override
    public List<CartVO> getMyCart() {
        Long userId = UserContext.getUserId();

        List<Cart> carts = super.list(
                new LambdaQueryWrapper<Cart>()
                        .eq(Cart::getUserId, userId)
        );

        return carts.stream()
                .map(cart -> {
                    CartVO vo = new CartVO();
                    BeanUtils.copyProperties(cart, vo);
                    vo.setTotalPrice(
                            cart.getPrice().multiply(
                                    BigDecimal.valueOf(cart.getQuantity())
                            )
                    );
                    return vo;
                })
                .collect(Collectors.toList());
    }

    @Override
    public void update(Long id, Integer quantity) {
        Long userId = UserContext.getUserId();
        Cart cart = this.getById(id);
        if (cart == null || !cart.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        cart.setQuantity(quantity);
        this.updateById(cart);
    }

    @Override
    public void delete(Long id) {
        Long userId = UserContext.getUserId();
        Cart cart = this.getById(id);
        if (cart == null || !cart.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        this.removeById(id);
    }
}
