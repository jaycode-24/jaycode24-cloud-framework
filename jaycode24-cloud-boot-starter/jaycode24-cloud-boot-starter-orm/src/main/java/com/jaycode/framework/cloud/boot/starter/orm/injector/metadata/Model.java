package com.jaycode.framework.cloud.boot.starter.orm.injector.metadata;

import cn.hutool.core.collection.CollUtil;
import com.jaycode.framework.cloud.boot.core.entity.ColumnProperty;
import com.jaycode.framework.cloud.boot.core.entity.EntityManager;
import com.jaycode.framework.cloud.boot.core.entity.EntityMeta;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Data
public class Model {
    //主键字段名
    private String primaryKeyColumnName = "id";
    //逻辑删除字段名
    private String archivePrimaryColumnName = "is_deleted";

    private String tableName = "";

    private Class<?> modelClass;

    private List<ModelField> columns = new ArrayList<>();

    private static final EntityManager ENTITY_MANAGER = EntityManager.getInstance();

    private EntityMeta entityMeta;


    public Model(Class<?> modelClass) {
        if (ENTITY_MANAGER.isEntity(modelClass)) {
            this.modelClass = modelClass;
            this.entityMeta = ENTITY_MANAGER.getEntityMeta(modelClass);
            this.tableName = entityMeta.getTableName();
            List<ColumnProperty> columnProperties = entityMeta.getAllColumnFields();
            if (!CollUtil.isEmpty(columnProperties)) {
                columnProperties.forEach((item) -> {
                    this.columns.add(new ModelField(item.getName(), item.getField().getName()));
                });
            }
            if (entityMeta.getPrimaryArchiveColumnField() != null) {
                archivePrimaryColumnName = entityMeta.getPrimaryArchiveColumnField().getName();
            }
            primaryKeyColumnName = entityMeta.getIdColumnField().getName();
        }
        //解析field column
    }

    public String fetchTableFields(Predicate<ColumnProperty> predicate) {
        if (modelClass == null) {
            return "";
        }
        List<ColumnProperty> columnProperties = entityMeta.getAllColumnFields();
        StringBuilder stringBuilder = new StringBuilder();
        columnProperties.stream().filter(predicate).forEach((item) -> {
            stringBuilder.append(item.getName()).append(",");
        });
        return stringBuilder.substring(0, stringBuilder.length() - 1);
    }

    public String fetchObjectValues(Predicate<ColumnProperty> predicate) {
        if (modelClass == null) {
            return "";
        }
        List<ColumnProperty> columnProperties = entityMeta.getAllColumnFields();
        StringBuilder stringBuilder = new StringBuilder();
        columnProperties.stream().filter(predicate).forEach((item) -> {
            stringBuilder.append("#{").append(item.getField().getName()).append("}").append(",");
        });
        return stringBuilder.substring(0, stringBuilder.length() - 1);
    }

    public EntityMeta getEntityMeta() {
        return entityMeta;
    }

}
