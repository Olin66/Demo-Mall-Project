package com.mall.seckill.scheduled;

import com.mall.common.constant.SeckillConstant;
import com.mall.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class SeckillSkuScheduled {

    @Autowired
    SeckillService seckillService;

    @Autowired
    RedissonClient redissonClient;

    @Scheduled(cron = "*/3 * * * * ?")
    public void uploadSeckillSkuLatestThreeDays() {
        RLock lock = redissonClient.getLock(SeckillConstant.UPLOAD_LOCK);
        lock.lock(1, TimeUnit.MINUTES);
        try {
            seckillService.uploadSeckillSkuLatestThreeDays();
        } finally {
            lock.unlock();
        }
    }
}
