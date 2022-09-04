package com.mall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.common.utils.PageUtils;
import com.mall.ware.entity.WareInfoEntity;
import com.mall.ware.vo.FareVo;

import java.util.Map;

/**
 * 仓库信息
 *
 * @author SnowCharm
 * @email 619022098@qq.com
 * @date 2022-07-10 19:48:36
 */
public interface WareInfoService extends IService<WareInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    FareVo getFare(Long addrId);
}

