package com.redis.monitor.exception;

public class ConflictsException extends Exception {
    public ConflictsException() {
    }

    public ConflictsException(String msg) {
        super(msg);
    }

    public ConflictsException(String msg, Throwable t) {
        super(msg, t);
    }
}
