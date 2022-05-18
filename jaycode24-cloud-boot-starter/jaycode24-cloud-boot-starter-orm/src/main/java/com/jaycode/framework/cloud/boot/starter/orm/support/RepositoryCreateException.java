package com.jaycode.framework.cloud.boot.starter.orm.support;


import com.jaycode.framework.cloud.boot.core.CloudFrameworkException;

public class RepositoryCreateException extends CloudFrameworkException {
    public RepositoryCreateException() {
    }

    public RepositoryCreateException(String message) {
        super(message);
    }

    public RepositoryCreateException(String message, Throwable cause) {
        super(message, cause);
    }
}
