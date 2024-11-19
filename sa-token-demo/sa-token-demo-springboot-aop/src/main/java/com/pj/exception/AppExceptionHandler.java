package com.pj.exception;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AppExceptionHandler {

    private static final Logger log = LogManager.getLogger(AppExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public String handler(Exception e) {
        log.error("some error", e);
        return e.getMessage();
    }
}
