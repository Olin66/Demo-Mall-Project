package com.mall.common.exception;

public enum ExceptionCodeEnum {
    UNKNOWN_EXCEPTION(10000, "系统未知异常"),
    VALID_EXCEPTION(10001, "参数格式校验失败"),
    SMS_CODE_EXCEPTION(10002, "短信验证码频率过高"),
    PRODUCT_UP_EXCEPTION(11000, "商品上架异常"),
    USERNAME_EXISTED_EXCEPTION(15001, "用户名已经存在"),
    PHONE_EXISTED_EXCEPTION(15002, "手机号已经存在");


    private final int code;
    private final String message;

    ExceptionCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
