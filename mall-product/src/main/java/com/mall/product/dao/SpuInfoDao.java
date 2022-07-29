package com.mall.product.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.product.entity.SpuInfoEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * spu信息
 * 
 * @author SnowCharm
 * @email 619022098@qq.com
 * @date 2022-07-10 17:40:23
 */
@Mapper
public interface SpuInfoDao extends BaseMapper<SpuInfoEntity> {

    void updateSpuStatus(@Param("spuId") Long spuId, @Param("code") int code);
}
