package com.jaycode.framework.cloud.boot.starter.orm.entity;

import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.springframework.core.annotation.Order;

import java.sql.Statement;
import java.util.Properties;

/**
 * Mybatis 查询结果集处理拦截器,仅支持resultType类型，业务应用可通过@Column方式直接指定属性配置关系
 *  * 亦支持mybatis原生基于属性名直接映射的策略，注意无论是基于Column还是PropertyName都是忽略大小写
 *  * 重写基于类查询返回对象实现，兼容多数据库，以及支持自定义注解
 * @author cheng.wang
 * @date 2022/5/16
 */
@Intercepts(@Signature(
        type = ResultHandler.class,method = "handleResultSets",args = Statement.class
))
@Order(3)
public class EntityResultInterceptor implements Interceptor {
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
