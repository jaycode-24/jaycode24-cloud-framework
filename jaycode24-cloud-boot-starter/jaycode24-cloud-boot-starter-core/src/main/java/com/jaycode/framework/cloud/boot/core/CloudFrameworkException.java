package com.jaycode.framework.cloud.boot.core;

/**
 * 微服务框架异常，通常在组件层抛出
 *
 * @author jinlong.wang
 */
public class CloudFrameworkException extends CloudException {

    private static final long serialVersionUID = -2250708764220548279L;

    public CloudFrameworkException() {
    }

    public CloudFrameworkException(String message) {
        super(message);
    }

    public CloudFrameworkException(String message, Throwable cause) {
        super(message, cause);
    }
}
