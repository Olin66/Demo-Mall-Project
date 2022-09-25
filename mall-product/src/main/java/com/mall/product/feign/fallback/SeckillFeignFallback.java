package com.mall.product.feign.fallback;

import com.mall.common.exception.ExceptionCodeEnum;
import com.mall.common.utils.R;
import com.mall.product.feign.SeckillFeignService;
import org.springframework.stereotype.Component;

@Component
public class SeckillFeignFallback implements SeckillFeignService {
    @Override
    public R getSkuSeckillInfo(Long skuId) {
        return R.error(ExceptionCodeEnum.TOO_MANY_REQUEST_EXCEPTION.getCode(),
                ExceptionCodeEnum.TOO_MANY_REQUEST_EXCEPTION.getMessage());
    }
}
