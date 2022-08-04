package com.mall.search.vo;

import lombok.Data;

import java.util.List;

@Data
public class SearchParamVo {
    private String keyword;
    private Long catalog3Id;
    private String sort;
    private Integer hasStock = 1;
    private String skuPrice;
    private List<Long> brandId;
    private List<String> attrs;
    Integer pageNum = 1;
}
