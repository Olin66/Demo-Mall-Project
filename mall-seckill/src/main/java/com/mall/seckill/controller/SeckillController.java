package com.mall.seckill.controller;

import com.mall.common.utils.R;
import com.mall.seckill.service.SeckillService;
import com.mall.seckill.vo.SeckillSkuRedisVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SeckillController {

    @Autowired
    SeckillService seckillService;

    @GetMapping("/currentSeckillSkus")
    public R getCurrentSeckillSkus() {
        List<SeckillSkuRedisVo> vos = seckillService.getCurrentSeckillSkus();
        return R.ok().put("data", vos);
    }

    @GetMapping("/sku/seckill/{skuId}")
    public R getSkuSeckillInfo(@PathVariable("skuId") Long skuId) {
        SeckillSkuRedisVo vo = seckillService.getSkuSeckillInfo(skuId);
        return R.ok().put("data", vo);
    }
}
