package com.jaycode.framework.cloud.boot.core.bean.md;


import com.jaycode.framework.cloud.boot.core.CloudFrameworkException;

public class ModuleDefineException extends CloudFrameworkException {
    public ModuleDefineException() {
    }

    public ModuleDefineException(String message) {
        super(message);
    }

    public ModuleDefineException(String message, Throwable cause) {
        super(message, cause);
    }
}
