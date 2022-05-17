package com.jaycode.framework.cloud.boot.starter.orm.entity;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.springframework.core.annotation.Order;

import java.util.Properties;

/**
 * 基于Mybatis 实现乐观锁
 * @author cheng.wang
 * @date 2022/5/16
 */
@Intercepts(@Signature(
        type = Executor.class,method = "update",args = {MappedStatement.class, Object.class}
))
@Order(2)
public class VersionLockInterceptor implements Interceptor {
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        return null;
    }

    @Override
    public Object plugin(Object target) {
        return null;
    }

    @Override
    public void setProperties(Properties properties) {

    }
}
