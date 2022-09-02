package com.mall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mall.cart.feign.ProductFeignService;
import com.mall.cart.interceptor.CartInterceptor;
import com.mall.cart.service.CartService;
import com.mall.cart.vo.CartItemVo;
import com.mall.cart.vo.SkuInfoVo;
import com.mall.common.constant.CartConstant;
import com.mall.common.to.UserInfoTo;
import com.mall.common.utils.R;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

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
    public CartItemVo addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {
        CartItemVo vo;
        BoundHashOperations<String, Object, Object> operations = getCartOps();
        String result = (String) operations.get(skuId.toString());
        if (StringUtils.isEmpty(result)){
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
        return vo;
    }

    private BoundHashOperations<String, Object, Object> getCartOps() {
        UserInfoTo user = CartInterceptor.threadLocal.get();
        String cartKey;
        if (user.getUserKey() != null) {
            cartKey = CartConstant.CART_PREFIX + user.getUserId();
        } else {
            cartKey = CartConstant.CART_PREFIX + user.getUserKey();
        }
        return redisTemplate.boundHashOps(cartKey);
    }
}
