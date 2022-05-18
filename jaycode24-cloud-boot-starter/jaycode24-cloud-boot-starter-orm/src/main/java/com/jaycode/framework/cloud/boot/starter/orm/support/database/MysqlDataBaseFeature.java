package com.jaycode.framework.cloud.boot.starter.orm.support.database;

import com.jaycode.framework.cloud.boot.starter.orm.page.MybatisPagination;

/**
 * @author cheng.wang
 * @date 2022/5/17
 */
public class MysqlDataBaseFeature extends BaseDatabaseFeature{
    @Override
    public String getProductName() {
        return "mysql";
    }

    /*@Override
    public String getPageQuerySql(String sql, MybatisPagination page) {
        return sql + "limit" + page.getS;
    }*/

    @Override
    public String getPageCountSql(String sql) {
        return null;
    }

    @Override
    public String getLimitSql(String sql, int i) {
        return null;
    }

    @Override
    public String getDateComparatorFuncSql(String dateInput) {
        return null;
    }

    @Override
    public String getFindInSetFuncSql(String value, String columnName) {
        return null;
    }
}
