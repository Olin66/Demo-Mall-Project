package com.mall.product.vo.pojo;

import lombok.Data;

import java.util.List;

@Data
public class SpuAttrGroup {
    private String groupName;
    private List<SpuBaseAttr> attrs;
}
