package com.mall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CatalogSecondVo {
    private String catelog1Id;
    private List<CatalogThirdVo> catalog3List;
    private String id;
    private String name;
}
