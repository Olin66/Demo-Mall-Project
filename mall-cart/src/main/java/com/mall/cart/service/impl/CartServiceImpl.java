package com.mall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mall.cart.feign.ProductFeignService;
import com.mall.cart.interceptor.CartInterceptor;
import com.mall.cart.service.CartService;
import com.mall.cart.vo.CartItemVo;
import com.mall.cart.vo.CartVo;
import com.mall.cart.vo.SkuInfoVo;
import com.mall.common.constant.CartConstant;
import com.mall.common.to.UserInfoTo;
import com.mall.common.utils.R;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    ThreadPoolExecutor executor;

    @Override
    public void addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {
        CartItemVo vo;
        BoundHashOperations<String, Object, Object> operations = getCartOps();
        String result = (String) operations.get(skuId.toString());
        if (StringUtils.isEmpty(result)) {
            vo = new CartItemVo();
            CompletableFuture<Void> skuInfo = CompletableFuture.runAsync(() -> {
                R r = productFeignService.info(skuId);
                String s = JSON.toJSONString(r.get("skuInfo"));
                SkuInfoVo data = JSONObject.parseObject(s, SkuInfoVo.class);
                vo.setCheck(true);
                vo.setCount(num);
                vo.setSkuId(skuId);
                vo.setImage(data.getSkuDefaultImg());
                vo.setTitle(data.getSkuTitle());
                vo.setPrice(data.getPrice());
            }, executor);
            CompletableFuture<Void> skuSaleAttr = CompletableFuture.runAsync(() -> {
                List<String> values = productFeignService.getSkuSakeAttrValues(skuId);
                vo.setSkuAttr(values);
            }, executor);
            CompletableFuture.allOf(skuInfo, skuSaleAttr).get();
            String s = JSON.toJSONString(vo);
            operations.put(skuId.toString(), s);
        } else {
            vo = JSONObject.parseObject(result, CartItemVo.class);
            vo.setCount(vo.getCount() + num);
            String s = JSON.toJSONString(vo);
            operations.put(skuId.toString(), s);
        }
    }

    @Override
    public CartItemVo getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> operations = getCartOps();
        String o = (String) operations.get(skuId.toString());
        CartItemVo vo = JSONObject.parseObject(o, CartItemVo.class);
        return JSONObject.parseObject(o, CartItemVo.class);
    }

    @Override
    public CartVo getCart() throws ExecutionException, InterruptedException {
        CartVo cart = new CartVo();
        UserInfoTo user = CartInterceptor.threadLocal.get();
        if (user.getUserId() != null) {
            List<CartItemVo> notLoginCartItems = getCartItems(CartConstant.CART_PREFIX + user.getUserKey());
            if (notLoginCartItems != null && !notLoginCartItems.isEmpty()) {
                for (CartItemVo vo : notLoginCartItems) {
                    addToCart(vo.getSkuId(), vo.getCount());
                }
                clearCart(CartConstant.CART_PREFIX + user.getUserKey());
            }
            List<CartItemVo> loginCartItems = getCartItems(CartConstant.CART_PREFIX + user.getUserId());
            cart.setItems(loginCartItems);
        } else {
            List<CartItemVo> cartItems = getCartItems(CartConstant.CART_PREFIX + user.getUserKey());
            cart.setItems(cartItems);
        }
        return cart;
    }

    @Override
    public void clearCart(String cartKey) {
        redisTemplate.delete(cartKey);
    }

    @Override
    public void checkItem(Long skuId, Integer check) {
        BoundHashOperations<String, Object, Object> operations = getCartOps();
        CartItemVo cartItem = getCartItem(skuId);
        cartItem.setCheck(check == 1);
        String json = JSON.toJSONString(cartItem);
        operations.put(skuId.toString(), json);
    }

    @Override
    public void countItem(Long skuId, Integer num) {
        BoundHashOperations<String, Object, Object> operations = getCartOps();
        CartItemVo cartItem = getCartItem(skuId);
        cartItem.setCount(num);
        String json = JSON.toJSONString(cartItem);
        operations.put(skuId.toString(), json);
    }

    @Override
    public void deleteItem(Long skuId) {
        BoundHashOperations<String, Object, Object> operations = getCartOps();
        operations.delete(skuId.toString());
    }

    @Override
    public List<CartItemVo> getUserCartItems() {
        UserInfoTo user = CartInterceptor.threadLocal.get();
        if (user.getUserId() == null) return null;
        String cartKey = CartConstant.CART_PREFIX + user.getUserId();
        List<CartItemVo> list = getCartItems(cartKey);
        if (list == null) return null;
        return list.stream()
                .filter(CartItemVo::getCheck)
                .peek(item -> {
                    BigDecimal price = productFeignService.getPrice(item.getSkuId());
                    item.setPrice(price);
                }).toList();
    }

    private BoundHashOperations<String, Object, Object> getCartOps() {
        UserInfoTo user = CartInterceptor.threadLocal.get();
        String cartKey;
        if (user.getUserId() != null) {
            cartKey = CartConstant.CART_PREFIX + user.getUserId();
        } else {
            cartKey = CartConstant.CART_PREFIX + user.getUserKey();
        }
        return redisTemplate.boundHashOps(cartKey);
    }

    private List<CartItemVo> getCartItems(String cartKey) {
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(cartKey);
        List<Object> values = operations.values();
        if (values != null && !values.isEmpty()) {
            return values.stream().map(obj -> {
                String str = (String) obj;
                return JSONObject.parseObject(str, CartItemVo.class);
            }).toList();
        } else return null;
    }

}
