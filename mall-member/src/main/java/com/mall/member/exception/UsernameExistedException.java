package com.mall.member.exception;

public class UsernameExistedException extends RuntimeException {
    public UsernameExistedException() {
        super("用户名存在");
    }
}
