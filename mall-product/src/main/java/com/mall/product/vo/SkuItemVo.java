package com.mall.product.vo;

import com.mall.product.entity.SkuImagesEntity;
import com.mall.product.entity.SkuInfoEntity;
import com.mall.product.entity.SpuInfoDescEntity;
import com.mall.product.vo.pojo.SkuSaleAttr;
import com.mall.product.vo.pojo.SpuAttrGroup;
import lombok.Data;

import java.util.List;

@Data
public class SkuItemVo {
    SkuInfoEntity info;
    Boolean hasStock = true;
    List<SkuImagesEntity> images;
    List<SkuSaleAttr> saleAttr;
    List<SpuAttrGroup> groupAttrs;
    SpuInfoDescEntity desc;
    SeckillInfoVo seckillInfo;
}
