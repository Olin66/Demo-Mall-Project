package com.mall.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.constant.OrderConstant;
import com.mall.common.to.SkuHasStockTo;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.Query;
import com.mall.common.utils.R;
import com.mall.common.vo.MemberRespVo;
import com.mall.order.dao.OrderDao;
import com.mall.order.entity.OrderEntity;
import com.mall.order.feign.CartFeignService;
import com.mall.order.feign.MemberFeignService;
import com.mall.order.feign.WareFeignService;
import com.mall.order.interceptor.LoginUserInterceptor;
import com.mall.order.service.OrderService;
import com.mall.order.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    ThreadPoolExecutor executor;

    @Autowired
    MemberFeignService memberFeignService;

    @Autowired
    CartFeignService cartFeignService;

    @Autowired
    WareFeignService wareFeignService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {
        OrderConfirmVo vo = new OrderConfirmVo();
        MemberRespVo memberRespVo = LoginUserInterceptor.loginUser.get();
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        CompletableFuture<Void> addressTask = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(attributes);
            List<OrderAddressVo> addresses = memberFeignService.getAddresses(memberRespVo.getId());
            vo.setAddresses(addresses);
        }, executor);
        CompletableFuture<Void> itemTask = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(attributes);
            List<OrderItemVo> items = cartFeignService.getCurrentUserCartItems();
            vo.setItems(items);
        }, executor).thenRunAsync(() -> {
            List<OrderItemVo> items = vo.getItems();
            List<Long> list = items.stream().map(OrderItemVo::getSkuId).toList();
            R r = wareFeignService.getSkusHasStock(list);
            String json = JSON.toJSONString(r.get("data"));
            JSONArray array = JSONArray.parseArray(json);
            List<SkuHasStockTo> data = array.toJavaList(SkuHasStockTo.class);
            if (data != null) {
                Map<Long, Boolean> map = data.stream()
                        .collect(Collectors.toMap(SkuHasStockTo::getSkuId, SkuHasStockTo::getHasStock));
                vo.setStocks(map);
            }
        }, executor);
        CompletableFuture<Void> integrationTask = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(attributes);
            Integer integration = memberRespVo.getIntegration();
            vo.setIntegration(integration);
        }, executor);
        String token = UUID.randomUUID().toString().replace("-", "");
        vo.setOrderToken(token);
        redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId(),
                token, 30, TimeUnit.MINUTES);
        CompletableFuture.allOf(addressTask, itemTask, integrationTask).get();
        return vo;
    }

    @Override
    public OrderSubmitRespVo submitOrder(OrderSubmitVo vo) {
        return null;
    }

}