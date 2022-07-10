package com.mall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.common.utils.PageUtils;
import com.mall.order.entity.MqMessageEntity;

import java.util.Map;

/**
 * 
 *
 * @author SnowCharm
 * @email 619022098@qq.com
 * @date 2022-07-10 19:44:24
 */
public interface MqMessageService extends IService<MqMessageEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

