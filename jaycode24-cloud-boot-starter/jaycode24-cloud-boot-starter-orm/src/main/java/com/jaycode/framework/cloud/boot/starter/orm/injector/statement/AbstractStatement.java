package com.jaycode.framework.cloud.boot.starter.orm.injector.statement;


import com.jaycode.framework.cloud.boot.starter.orm.injector.metadata.Model;
import com.jaycode.framework.cloud.boot.starter.orm.injector.statement.anotation.SqlStatement;

/**
 * 自定义Statement模板类
 *
 * @author jinlong.wang
 */
public abstract class AbstractStatement {
    protected Model modelMetaData;
    protected Class<?> repositoryClass;

    public AbstractStatement(Model modelMetaData, Class<?> repositoryClass) {
        this.modelMetaData = modelMetaData;
        this.repositoryClass = repositoryClass;
    }

    public abstract String sql();

    public String id() {
        SqlStatement sqlStatement = this.getClass().getAnnotation(SqlStatement.class);
        return repositoryClass.getName() + "." + sqlStatement.value();
    }

    public Model getModelMetaData() {
        return modelMetaData;
    }
}
