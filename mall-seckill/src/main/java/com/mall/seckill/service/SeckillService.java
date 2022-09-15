package com.mall.seckill.service;

import com.mall.seckill.vo.SeckillSkuRedisVo;

import java.util.List;

public interface SeckillService {
    void uploadSeckillSkuLatestThreeDays();

    List<SeckillSkuRedisVo> getCurrentSeckillSkus();

    SeckillSkuRedisVo getSkuSeckillInfo(Long skuId);

    String kill(String killId, String code, Integer num);
}
