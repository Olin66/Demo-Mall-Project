package com.mall.order.vo;

import lombok.Data;

import java.util.List;

@Data
public class OrderSkuLockVo {
    private String orderSn;
    private List<OrderItemVo> locks;
}
