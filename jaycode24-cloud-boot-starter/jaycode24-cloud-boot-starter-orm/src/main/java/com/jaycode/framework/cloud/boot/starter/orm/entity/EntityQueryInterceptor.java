package com.jaycode.framework.cloud.boot.starter.orm.entity;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.core.annotation.Order;

import java.util.Properties;

/**
 *
 *  * 分页插件，实现Mybatis 分页
 *  * 基于BaseRepository selectByQuery实现
 *  * todo 每次查询的时候优先获取一次连接，如果是动态路由因绑定了相同的sqlSession，所以事务管理器相同，
 *  * 事务管理器在一个线程中作为单例存在，在事务中切换路由会失效，导致获取错误的Connection
 *  * 但是当前架构不存在多写多个业务库的设计，所以该异常暂时忽略
 * @author cheng.wang
 * @date 2022/5/16
 */
@Intercepts(@Signature(type = Executor.class,method = "query",
            args = {MappedStatement.class,Object.class, RowBounds.class, ResultHandler.class}))
@Order(0)
public class EntityQueryInterceptor implements Interceptor {
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
