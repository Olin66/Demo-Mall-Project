package com.mall.ware.vo;

import lombok.Data;

import java.util.List;

@Data
public class SkuWareHasStockVo {
    private Long skuId;
    private Integer num;
    private List<Long> wareIds;
}
