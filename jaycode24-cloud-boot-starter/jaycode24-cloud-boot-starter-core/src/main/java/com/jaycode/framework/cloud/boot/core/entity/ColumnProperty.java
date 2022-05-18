package com.jaycode.framework.cloud.boot.core.entity;

import com.funi.framework.cloud.boot.core.classmeta.FieldDecorate;
import org.springframework.util.StringUtils;

import javax.persistence.Column;
import java.lang.reflect.Field;

public class ColumnProperty {
    private final Field field;
    private final Column column;


    public ColumnProperty(FieldDecorate fieldDecorate) {
        this.field = fieldDecorate.getField();
        this.column = (Column) fieldDecorate.getDecorate();
    }

    public ColumnProperty(Field field, Column column) {
        this.field = field;
        this.column = column;
    }

    public Field getField() {
        return field;
    }


    public Column getColumn() {
        return column;
    }

    /**
     * 获取字段名
     *
     * @return 字段名
     */
    public String getName() {
        return StringUtils.isEmpty(column.name()) ? field.getName() : column.name();
    }

}
