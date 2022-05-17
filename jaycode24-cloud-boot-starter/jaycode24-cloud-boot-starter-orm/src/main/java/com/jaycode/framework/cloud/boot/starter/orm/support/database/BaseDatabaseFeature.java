package com.jaycode.framework.cloud.boot.starter.orm.support.database;


import com.jaycode.framework.cloud.boot.starter.orm.support.DatabaseFeature;

public abstract class BaseDatabaseFeature implements DatabaseFeature {
    @Override
    public String getValidationQuerySql() {
        return "select 1";
    }


}
