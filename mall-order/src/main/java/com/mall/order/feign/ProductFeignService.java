package com.mall.order.feign;

import com.mall.order.vo.OrderSpuInfoVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("mall-product")
public interface ProductFeignService {
    @GetMapping("/product/spuinfo/sku/{skuId}")
    OrderSpuInfoVo getSpuInfoBySkuId(@PathVariable("skuId") Long skuId);
}
