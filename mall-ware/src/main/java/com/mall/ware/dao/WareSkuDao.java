package com.mall.ware.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.ware.entity.WareSkuEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品库存
 *
 * @author SnowCharm
 * @email 619022098@qq.com
 * @date 2022-07-10 19:48:36
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    void addStack(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("skuNum") Integer skuNum);

    Long getSkuStock(Long sku);

    List<Long> listWareIdHasSkuStock(@Param("skuId") Long skuId);

    Long lockSkuStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("num") Integer num);

    void unlockStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("num") Integer num);
}
