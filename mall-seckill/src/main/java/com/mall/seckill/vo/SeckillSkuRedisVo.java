package com.mall.seckill.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SeckillSkuRedisVo {
    private Long id;
    private Long promotionId;
    private Long promotionSessionId;
    private Long skuId;
    private BigDecimal seckillPrice;
    private Integer seckillCount;
    private Integer seckillLimit;
    private Integer seckillSort;
    private SeckillSkuInfoVo skuInfo;
    private Long startTime;
    private Long endTime;
    private String randomCode;
}
