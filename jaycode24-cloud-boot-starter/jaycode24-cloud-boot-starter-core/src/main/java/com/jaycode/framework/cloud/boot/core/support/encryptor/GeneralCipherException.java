package com.jaycode.framework.cloud.boot.core.support.encryptor;


import com.jaycode.framework.cloud.boot.core.CloudFrameworkException;

public class GeneralCipherException extends CloudFrameworkException {
    public GeneralCipherException() {
    }

    public GeneralCipherException(String message) {
        super(message);
    }

    public GeneralCipherException(String message, Throwable cause) {
        super(message, cause);
    }
}
