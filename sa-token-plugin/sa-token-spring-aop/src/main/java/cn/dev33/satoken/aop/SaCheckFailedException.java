package cn.dev33.satoken.aop;

import cn.dev33.satoken.exception.SaTokenException;

public class SaCheckFailedException extends SaTokenException {
    public SaCheckFailedException(int code) {
        super(code);
    }
}
