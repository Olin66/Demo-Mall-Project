package com.mall.cart.controller;

import com.mall.cart.interceptor.CartInterceptor;
import com.mall.common.to.UserInfoTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CartController {
    @GetMapping("/cart.html")
    public String cartListPage() {
        UserInfoTo user = CartInterceptor.threadLocal.get();
        return "cartList";
    }
}
