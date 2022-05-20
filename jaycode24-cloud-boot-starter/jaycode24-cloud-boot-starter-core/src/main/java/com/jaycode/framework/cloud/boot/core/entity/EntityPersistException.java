package com.jaycode.framework.cloud.boot.core.entity;

import com.jaycode.framework.cloud.boot.core.CloudFrameworkException;

public class EntityPersistException extends CloudFrameworkException {
    public EntityPersistException() {
    }

    public EntityPersistException(String message) {
        super(message);
    }

    public EntityPersistException(String message, Throwable cause) {
        super(message, cause);
    }
}
