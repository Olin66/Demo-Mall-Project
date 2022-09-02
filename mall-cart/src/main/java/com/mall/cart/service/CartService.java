package com.mall.cart.service;

import com.mall.cart.vo.CartItemVo;

import java.util.concurrent.ExecutionException;

public interface CartService {
    void addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException;

    CartItemVo getCartItem(Long skuId);
}
