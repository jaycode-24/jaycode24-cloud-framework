package com.jaycode.framework.cloud.boot.starter.orm.injector.metadata;

import lombok.Data;

@Data
public class ModelField {
    private String columnName;
    private String propertyName;

    public ModelField(String columnName, String propertyName) {
        this.columnName = columnName;
        this.propertyName = propertyName;
    }
}
