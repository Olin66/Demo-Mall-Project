package com.mall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderFareVo {
    private OrderAddressVo address;
    private BigDecimal fare;
}
