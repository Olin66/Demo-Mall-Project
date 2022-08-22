package com.mall.product.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.product.entity.SkuSaleAttrValueEntity;
import com.mall.product.vo.pojo.SkuSaleAttr;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * sku销售属性&值
 * 
 * @author SnowCharm
 * @email 619022098@qq.com
 * @date 2022-07-10 17:40:24
 */
@Mapper
public interface SkuSaleAttrValueDao extends BaseMapper<SkuSaleAttrValueEntity> {

    List<SkuSaleAttr> getSaleAttrsBySpuId(@Param("spuId") Long spuId);
}
