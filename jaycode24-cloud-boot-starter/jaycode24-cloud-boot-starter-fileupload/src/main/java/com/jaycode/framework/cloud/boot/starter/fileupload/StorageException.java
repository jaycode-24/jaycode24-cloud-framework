package com.jaycode.framework.cloud.boot.starter.fileupload;


import com.jaycode.framework.cloud.boot.core.CloudException;

public class StorageException extends CloudException {
    public StorageException() {
    }

    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
