package com.jaycode.framework.cloud.boot.core;

/**
 * @author cheng.wang
 * @date 2022/5/13
 */
public class CloudApplicationBootException extends CloudException{
    private static final long serialVersionUID = -6836777242083041417L;

    public CloudApplicationBootException(String message){
        super(message);
    }

    public CloudApplicationBootException(String message,Throwable cause){
        super(message,cause);
    }
}
