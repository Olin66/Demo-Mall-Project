package com.mall.product.vo.pojo;

import lombok.Data;

import java.util.List;

@Data
public class SkuSaleAttr {
    private Long attrId;
    private String attrName;
    private List<AttrValueWithSkuId> attrValues;
}
