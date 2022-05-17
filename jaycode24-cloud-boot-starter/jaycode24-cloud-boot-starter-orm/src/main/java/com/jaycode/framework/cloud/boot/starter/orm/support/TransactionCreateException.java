package com.jaycode.framework.cloud.boot.starter.orm.support;


import com.jaycode.framework.cloud.boot.core.CloudFrameworkException;

public class TransactionCreateException extends CloudFrameworkException {
    public TransactionCreateException() {
    }

    public TransactionCreateException(String message) {
        super(message);
    }

    public TransactionCreateException(String message, Throwable cause) {
        super(message, cause);
    }
}
