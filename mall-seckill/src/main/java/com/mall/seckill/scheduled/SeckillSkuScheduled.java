package com.mall.seckill.scheduled;

import com.mall.seckill.service.SeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class SeckillSkuScheduled {

    @Autowired
    SeckillService seckillService;

    @Scheduled(cron = "0 0 3 * * ?")
    public void uploadSeckillSkuLatestThreeDays(){
        seckillService.uploadSeckillSkuLatestThreeDays();
    }
}
