package com.mall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.common.to.SkuHasStockTo;
import com.mall.common.to.mq.StockLockedTo;
import com.mall.common.utils.PageUtils;
import com.mall.ware.entity.WareSkuEntity;
import com.mall.ware.vo.WareSkuLockVo;

import java.util.List;
import java.util.Map;

public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStack(Long skuId, Long wareId, Integer skuNum);

    List<SkuHasStockTo> getSkuHasStock(List<Long> skuIds);

    void orderLockStock(WareSkuLockVo vo);

    void unlockStock(StockLockedTo to);
}

