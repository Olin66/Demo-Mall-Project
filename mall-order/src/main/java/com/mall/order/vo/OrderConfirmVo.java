package com.mall.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderConfirmVo {
    List<OrderAddressVo> addresses;
    List<OrderItemVo> items;
    Integer integration;
    String orderToken;

    public BigDecimal getTotal() {
        BigDecimal total = new BigDecimal(0);
        if (items != null) {
            for (OrderItemVo item : items) {
                BigDecimal multiply = item.getPrice().multiply(new BigDecimal(item.getCount()));
                total = total.add(multiply);
            }
        }
        return total;
    }

    public BigDecimal getPayPrice() {
        return getTotal();
    }
}
