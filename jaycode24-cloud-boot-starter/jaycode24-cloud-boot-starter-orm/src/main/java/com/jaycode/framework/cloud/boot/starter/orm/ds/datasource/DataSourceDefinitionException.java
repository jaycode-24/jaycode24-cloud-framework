package com.jaycode.framework.cloud.boot.starter.orm.ds.datasource;


import com.jaycode.framework.cloud.boot.core.CloudFrameworkException;

/**
 * 数据源定义错误
 */
public class DataSourceDefinitionException extends CloudFrameworkException {
    public DataSourceDefinitionException(String message) {
        super(message);
    }

    public DataSourceDefinitionException() {
    }


    public DataSourceDefinitionException(String message, Throwable cause) {
        super(message, cause);
    }
}
