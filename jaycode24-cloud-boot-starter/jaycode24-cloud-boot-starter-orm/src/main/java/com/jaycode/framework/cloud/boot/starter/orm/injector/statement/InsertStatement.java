package com.jaycode.framework.cloud.boot.starter.orm.injector.statement;

import com.funi.framework.cloud.boot.starter.orm.injector.metadata.Model;
import com.funi.framework.cloud.boot.starter.orm.injector.statement.anotation.SqlStatement;
import org.apache.ibatis.mapping.SqlCommandType;

import static com.funi.framework.cloud.boot.starter.orm.injector.statement.InsertStatement.SQL_ID;

@SqlStatement(value = SQL_ID, commandType = SqlCommandType.INSERT, resultType = int.class)
public class InsertStatement extends AbstractStatement {

    public static final String SQL_ID = "insert";

    public InsertStatement(Model modelMetaData, Class<?> repositoryClass) {
        super(modelMetaData, repositoryClass);
    }

    @Override
    public String sql() {
        String insertFields = "(" + modelMetaData.fetchTableFields(c -> c.getColumn().insertable()) + ")";
        String insertValues = "(" + modelMetaData.fetchObjectValues(c -> c.getColumn().insertable()) + ")";
        return String.format("INSERT INTO %s %s VALUES %s", modelMetaData.getTableName(), insertFields, insertValues);
    }
}
