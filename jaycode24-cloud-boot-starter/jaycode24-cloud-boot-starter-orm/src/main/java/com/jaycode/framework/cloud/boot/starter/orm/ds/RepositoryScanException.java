package com.jaycode.framework.cloud.boot.starter.orm.ds;

public class RepositoryScanException extends RuntimeException {
    public RepositoryScanException() {
    }

    public RepositoryScanException(String message) {
        super(message);
    }

    public RepositoryScanException(String message, Throwable cause) {
        super(message, cause);
    }

    public RepositoryScanException(Throwable cause) {
        super(cause);
    }

    public RepositoryScanException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
