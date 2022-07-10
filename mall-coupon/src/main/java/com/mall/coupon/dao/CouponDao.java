package com.mall.coupon.dao;

import com.mall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author SnowCharm
 * @email 619022098@qq.com
 * @date 2022-07-10 18:24:22
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
