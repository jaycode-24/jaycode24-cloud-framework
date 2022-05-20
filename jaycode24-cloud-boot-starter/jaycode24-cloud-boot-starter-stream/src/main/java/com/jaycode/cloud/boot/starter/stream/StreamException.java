package com.jaycode.cloud.boot.starter.stream;


import com.jaycode.framework.cloud.boot.core.CloudFrameworkException;

public class StreamException extends CloudFrameworkException {
    public StreamException() {
    }

    public StreamException(String message) {
        super(message);
    }

    public StreamException(String message, Throwable cause) {
        super(message, cause);
    }
}
