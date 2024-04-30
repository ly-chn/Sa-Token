package com.pj.exception;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AppExceptionHandler {
    @ExceptionHandler(Exception.class)
    public String handler(Exception e) {
        e.printStackTrace();
        return e.getMessage();
    }
}
