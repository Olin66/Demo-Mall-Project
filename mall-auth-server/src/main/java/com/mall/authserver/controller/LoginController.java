package com.mall.authserver.controller;

import com.mall.authserver.feign.ThirdPartyFeignService;
import com.mall.common.constant.AuthConstant;
import com.mall.common.exception.ExceptionCodeEnum;
import com.mall.common.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController {

    @Autowired
    ThirdPartyFeignService thirdPartyFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @ResponseBody
    @GetMapping("/sms/sendcode")
    public R sendCode(@RequestParam("phone") String phone) {
        String redisCode = redisTemplate.opsForValue().get(AuthConstant.SMS_CODE_CACHE_PREFIX + phone);
        if (redisCode != null) {
            long l = Long.parseLong(redisCode.substring(7));
            if (System.currentTimeMillis() - l < 60000) {
                return R.error(ExceptionCodeEnum.SMS_CODE_EXCEPTION.getCode(),
                        ExceptionCodeEnum.SMS_CODE_EXCEPTION.getMessage());
            }
        }
        String code = UUID.randomUUID().toString().substring(0, 5);
        thirdPartyFeignService.sendCode(phone, code);
        code = code + System.currentTimeMillis();
        redisTemplate.opsForValue().set(AuthConstant.SMS_CODE_CACHE_PREFIX + phone, code, 5, TimeUnit.MINUTES);
        return R.ok();
    }
}
