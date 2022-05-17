package com.jaycode.framework.cloud.boot.starter.orm.page;

import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.springframework.core.annotation.Order;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Properties;

/**
 * 分页插件，实现Mybatis 分页
 * @author cheng.wang
 * @date 2022/5/16
 */
@Intercepts({
        @Signature(type = StatementHandler.class,method = "prepare",args = {Connection.class,Integer.class}),
        @Signature(type = ResultSetHandler.class,method = "handleResultSets",args = Statement.class)
})
@Order(4)
public class PaginationInterceptor implements Interceptor {
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
