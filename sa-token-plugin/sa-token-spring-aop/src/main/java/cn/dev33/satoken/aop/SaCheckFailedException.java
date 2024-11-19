package cn.dev33.satoken.aop;

import cn.dev33.satoken.exception.SaTokenException;

public class SaCheckFailedException extends SaTokenException {
    public SaCheckFailedException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
