package com.mall.member.exception;

public class PhoneExistedException extends RuntimeException {
    public PhoneExistedException() {
        super("手机号存在");
    }
}
