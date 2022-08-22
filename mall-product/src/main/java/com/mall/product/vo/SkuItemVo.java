package com.mall.product.vo;

import com.mall.product.entity.SkuImagesEntity;
import com.mall.product.entity.SkuInfoEntity;
import com.mall.product.entity.SpuInfoDescEntity;
import com.mall.product.vo.pojo.SkuItemAttr;
import com.mall.product.vo.pojo.SpuAttrGroup;
import lombok.Data;

import java.util.List;

@Data
public class SkuItemVo {
    SkuInfoEntity info;
    List<SkuImagesEntity> images;
    List<SkuItemAttr> saleAttr;
    List<SpuAttrGroup> groupAttrs;
    SpuInfoDescEntity desc;
}
