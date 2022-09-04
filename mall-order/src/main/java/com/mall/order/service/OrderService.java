package com.mall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.common.utils.PageUtils;
import com.mall.order.entity.OrderEntity;
import com.mall.order.vo.OrderConfirmVo;
import com.mall.order.vo.OrderSubmitRespVo;
import com.mall.order.vo.OrderSubmitVo;

import java.util.Map;
import java.util.concurrent.ExecutionException;

public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException;

    OrderSubmitRespVo submitOrder(OrderSubmitVo vo);
}

