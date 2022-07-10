package com.mall.order.dao;

import com.mall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author SnowCharm
 * @email 619022098@qq.com
 * @date 2022-07-10 19:44:24
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
