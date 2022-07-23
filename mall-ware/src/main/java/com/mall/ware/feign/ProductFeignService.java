package com.mall.ware.feign;

import com.mall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("mall-gateway")
public interface ProductFeignService {
    @RequestMapping("/api/product/skuinfo//info/{skuId}")
    R info(@PathVariable("skuId") Long skuId);
}
