package com.mall.product.exception;

import com.mall.common.exception.ExceptionCodeEnum;
import com.mall.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice(basePackages = "com.mall.product.controller")
public class ExceptionControllerAdvice {

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handleValidException(MethodArgumentNotValidException e) {
        log.error("数据校验出现问题----{}，异常类型----{}", e.getMessage(), e.getClass());
        Map<String, String> map = new HashMap<>();
        BindingResult bindingResult = e.getBindingResult();
        bindingResult.getFieldErrors().forEach(err -> map.put(err.getField(), err.getDefaultMessage()));
        return R.error(ExceptionCodeEnum.VALID_EXCEPTION.getCode(), ExceptionCodeEnum.VALID_EXCEPTION.getMessage()).put("data", map);
    }

    @ExceptionHandler(value = Throwable.class)
    public R handleException(Throwable e) {
        e.printStackTrace();
        return R.error(ExceptionCodeEnum.UNKNOWN_EXCEPTION.getCode(), ExceptionCodeEnum.UNKNOWN_EXCEPTION.getMessage());
    }
}
