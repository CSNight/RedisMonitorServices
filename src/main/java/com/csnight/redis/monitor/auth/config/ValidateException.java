package com.csnight.redis.monitor.auth.config;

import org.springframework.security.core.AuthenticationException;

public class ValidateException extends AuthenticationException {
    public ValidateException(String msg) {
        super(msg);
    }

    public ValidateException(String msg, Throwable t) {
        super(msg, t);
    }
}
