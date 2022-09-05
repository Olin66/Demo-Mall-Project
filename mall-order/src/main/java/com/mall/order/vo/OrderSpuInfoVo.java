package com.mall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderSpuInfoVo {
    private Long id;
    private String spuName;
    private String spuDescription;
    private Long catalogId;
    private Long brandId;
    private BigDecimal weight;
    private Integer publishStatus;
}
