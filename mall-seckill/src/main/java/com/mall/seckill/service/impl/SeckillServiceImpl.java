package com.mall.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.mall.common.constant.SeckillConstant;
import com.mall.common.utils.R;
import com.mall.seckill.feign.CouponFeignService;
import com.mall.seckill.feign.ProductFeignService;
import com.mall.seckill.service.SeckillService;
import com.mall.seckill.vo.SeckillSessionsWithSkusVo;
import com.mall.seckill.vo.SeckillSkuInfoVo;
import com.mall.seckill.vo.SeckillSkuRedisVo;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class SeckillServiceImpl implements SeckillService {

    @Autowired
    CouponFeignService couponFeignService;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedissonClient redissonClient;


    @Override
    public void uploadSeckillSkuLatestThreeDays() {
        R r = couponFeignService.getLatestThreeDaysSession();
        if (r.getCode() == 0) {
            String json = JSON.toJSONString(r.get("data"));
            JSONArray array = JSON.parseArray(json);
            List<SeckillSessionsWithSkusVo> list = array.toJavaList(SeckillSessionsWithSkusVo.class);
            saveSessionInfo(list);
            saveSessionSkuInfo(list);
        }
    }

    private void saveSessionInfo(List<SeckillSessionsWithSkusVo> list) {
        list.forEach(session -> {
            long startTime = session.getStartTime().getTime();
            long endTime = session.getEndTime().getTime();
            String key = SeckillConstant.SESSIONS_CACHE_PREFIX + startTime + "_" + endTime + "_" + session.getId();
            if (Boolean.FALSE.equals(stringRedisTemplate.hasKey(key))) {
                List<String> skuIds = session
                        .getRelationSkus().stream()
                        .map(item -> item.getPromotionSessionId() + "_" + item.getSkuId().toString()).toList();
                stringRedisTemplate.opsForList().leftPushAll(key, skuIds);
            }
        });
    }

    private void saveSessionSkuInfo(List<SeckillSessionsWithSkusVo> list) {
        list.forEach(session -> {
            BoundHashOperations<String, Object, Object> ops
                    = stringRedisTemplate.boundHashOps(SeckillConstant.SECKILL_SKU_CACHE);
            session.getRelationSkus().forEach(vo -> {
                String token = UUID.randomUUID().toString().replace("-", "");
                if (Boolean.FALSE.equals(ops.hasKey(vo.getPromotionSessionId().toString() + "_" + vo.getSkuId().toString()))) {
                    SeckillSkuRedisVo redisVo = new SeckillSkuRedisVo();
                    R r = productFeignService.getSkuInfo(vo.getSkuId());
                    if (r.getCode() == 0) {
                        String s = JSON.toJSONString(r.get("skuInfo"));
                        SeckillSkuInfoVo skuInfoVo = JSON.parseObject(s, SeckillSkuInfoVo.class);
                        redisVo.setSkuInfo(skuInfoVo);
                    }
                    BeanUtils.copyProperties(vo, redisVo);
                    redisVo.setStartTime(session.getStartTime().getTime());
                    redisVo.setEndTime(session.getEndTime().getTime());
                    redisVo.setRandomCode(token);
                    String json = JSON.toJSONString(redisVo);
                    ops.put(vo.getPromotionSessionId().toString() + "_" + vo.getSkuId().toString(), json);
                    RSemaphore semaphore = redissonClient.getSemaphore(SeckillConstant.SKU_STOCK_SEMAPHORE_PREFIX + token);
                    semaphore.trySetPermits(vo.getSeckillCount());
                }
            });
        });
    }
}
