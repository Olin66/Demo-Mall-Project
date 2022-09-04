package com.mall.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderConfirmVo {
    List<AddressVo> addresses;
    List<OrderItemVo> items;
    Integer integration;
    BigDecimal total;
    BigDecimal payPrice;
}
