package com.mall.cart.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CartController {
    @GetMapping("/cart.html")
    public String cartListPage() {
        return "cartList";
    }

    @GetMapping("/addToCart")
    public String addToCart(){
        return "success";
    }
}
