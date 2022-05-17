package com.jaycode.framework.cloud.boot.starter.orm.injector.statement.anotation;

import org.apache.ibatis.mapping.SqlCommandType;

import java.lang.annotation.*;

/**
 * @author cheng.wang
 * @date 2022/5/16
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface SqlStatement {
    String VAR_SQL_BUILDER_NAME = "entitySqlBuilder";

    String value();

    SqlCommandType commandType();

    Class resultType();
}
