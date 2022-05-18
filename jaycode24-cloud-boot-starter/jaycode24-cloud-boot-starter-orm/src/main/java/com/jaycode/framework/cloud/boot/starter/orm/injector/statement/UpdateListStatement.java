package com.jaycode.framework.cloud.boot.starter.orm.injector.statement;

import com.funi.framework.cloud.boot.core.entity.ColumnProperty;
import com.funi.framework.cloud.boot.core.entity.EntityMeta;
import com.funi.framework.cloud.boot.starter.orm.injector.SqlTemplate;
import com.funi.framework.cloud.boot.starter.orm.injector.metadata.Model;
import com.funi.framework.cloud.boot.starter.orm.injector.statement.anotation.SqlStatement;
import org.apache.ibatis.mapping.SqlCommandType;

import java.util.List;

@SqlStatement(value = "updateList", commandType = SqlCommandType.UPDATE, resultType = int.class)
public class UpdateListStatement extends AbstractStatement {
    private boolean allowNullableUpdate = false;


    public UpdateListStatement(Model modelMetaData, Class<?> repositoryClass) {
        super(modelMetaData, repositoryClass);
    }

    public void setAllowNullableUpdate(boolean allowNullableUpdate) {
        this.allowNullableUpdate = allowNullableUpdate;
    }

    @Override
    public String sql() {

        EntityMeta entityMeta = modelMetaData.getEntityMeta();
        StringBuilder sqlBuilder = new StringBuilder();
        StringBuilder subSelectFieldBuilder = new StringBuilder();
        if (entityMeta != null) {
            List<ColumnProperty> columnPropertyList = entityMeta.getAllColumnFields();
            for (ColumnProperty columnProperty : columnPropertyList) {
                //insert 不允许则更新也不能自动更新
                if (!columnProperty.getColumn().insertable()) {
                    continue;
                }
                //如果是ID字段，则直接列出即可
                if (columnProperty.equals(entityMeta.getIdColumnField())) {
                    subSelectFieldBuilder.append(String.format("#{item.%s} as %s", columnProperty.getField().getName(), columnProperty.getName())).append(",");
                    continue;
                }
                //如果字段属性为允许更新，则参与批量更新
                if (columnProperty.getColumn().updatable()) {
                    sqlBuilder.append(String.format("a.%s=b.%s", columnProperty.getName(), columnProperty.getName())).append(",");
                    //如果是version 则子查询按照version+1 as version 的方式查询结果，以被查询
                    if (columnProperty.equals(entityMeta.getVersionColumnField())) {
                        subSelectFieldBuilder.append(String.format("%s+1 as %<s", columnProperty.getName())).append(",");
                        continue;
                    }
                    if (!allowNullableUpdate) {
                        //columnProperty.getField().getName()
                        String ifNullSqlAdaptorContainer = "<if test=\"_databaseId =='mysql'\"> IFNULL%s </if><if test=\"_databaseId == 'oracle'\"> nvl%<s </if>";
                        subSelectFieldBuilder.append(
                                String.format(ifNullSqlAdaptorContainer,
                                        String.format("(#{item.%s},%s) as %<s", columnProperty.getField().getName(), columnProperty.getName())
                                )
                        ).append(",");
                    } else {
                        subSelectFieldBuilder.append(String.format("#{item.%s} as %s", columnProperty.getField().getName(), columnProperty.getName())).append(",");
                    }
                }
            }
            SqlTemplate sqlTemplate = SqlTemplate.read("updateList.tpl");
            String subSelectCondition = createSubSelectCondition(modelMetaData.getEntityMeta());
            String defaultSubSelectFields = subSelectFieldBuilder.substring(0, subSelectFieldBuilder.length() - 1);
            sqlTemplate.setVariable("subCondition", subSelectCondition);
            sqlTemplate.setVariable("subSelectFields", defaultSubSelectFields);
            sqlTemplate.setVariable("sets", sqlBuilder.toString());
            return sqlTemplate
                    .setVariable(modelMetaData)
                    .compile();

        }
        return "";

    }

    private String createSubSelectCondition(EntityMeta entityMeta) {
        StringBuilder condition = new StringBuilder(String.format("%s=#{item.%s}",
                modelMetaData.getPrimaryKeyColumnName(), entityMeta.getIdColumnField().getField().getName()));
        //如果定义了version，则自动加上version=v;
        if (entityMeta.getVersionColumnField() != null) {
            ColumnProperty version = entityMeta.getVersionColumnField();
            condition.append(String.format("<if test='item.%s!=null'> and %s=#{item.%s} </if>",
                    version.getField().getName(),
                    version.getName(),
                    version.getField().getName()));

        }
        return condition.toString();
    }

}
