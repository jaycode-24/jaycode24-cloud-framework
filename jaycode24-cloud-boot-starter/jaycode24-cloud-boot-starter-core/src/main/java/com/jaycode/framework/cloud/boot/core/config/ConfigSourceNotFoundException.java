package com.jaycode.framework.cloud.boot.core.config;

import com.jaycode.framework.cloud.boot.core.CloudFrameworkException;

/**
 * @author cheng.wang
 * @date 2022/5/15
 */
public class ConfigSourceNotFoundException extends CloudFrameworkException {
    public ConfigSourceNotFoundException() {
    }

    public ConfigSourceNotFoundException(String message) {
        super(message);
    }

    public ConfigSourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}