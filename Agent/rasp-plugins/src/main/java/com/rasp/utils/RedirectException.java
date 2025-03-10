package com.rasp.utils;

public class RedirectException extends RuntimeException {
    public RedirectException(String message) {
        super(message);
    }

    public StackTraceElement[] getStackTrace() {
        return new StackTraceElement[0];
    }
}
