package com.mall.order.vo;

import com.mall.order.entity.OrderEntity;
import lombok.Data;

@Data
public class OrderSubmitRespVo {
    private OrderEntity order;
    private Integer code;
}
