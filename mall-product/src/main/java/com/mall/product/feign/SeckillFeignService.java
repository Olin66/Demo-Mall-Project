package com.mall.product.feign;

import com.mall.common.utils.R;
import com.mall.product.feign.fallback.SeckillFeignFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "mall-seckill", fallback = SeckillFeignFallback.class)
public interface SeckillFeignService {

    @GetMapping("/sku/seckill/{skuId}")
    R getSkuSeckillInfo(@PathVariable("skuId") Long skuId);
}
