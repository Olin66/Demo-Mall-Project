package com.mall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.common.to.mq.SeckillOrderTo;
import com.mall.common.utils.PageUtils;
import com.mall.order.entity.OrderEntity;
import com.mall.order.vo.*;

import java.util.Map;
import java.util.concurrent.ExecutionException;

public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException;

    OrderSubmitRespVo submitOrder(OrderSubmitVo vo);

    OrderEntity getOrderByOrderSn(String orderSn);

    void closeOrder(OrderCreateVo entity);

    PayVo getOrderPayVo(String orderSn);

    PageUtils queryPageWithItem(Map<String, Object> params);

    String handlePayResult(PayAsyncVo vo);

    void createSeckillOrder(SeckillOrderTo to);
}

