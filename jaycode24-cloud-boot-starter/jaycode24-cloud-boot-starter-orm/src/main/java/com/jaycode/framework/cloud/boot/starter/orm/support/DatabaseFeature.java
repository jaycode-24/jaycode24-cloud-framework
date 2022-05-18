package com.jaycode.framework.cloud.boot.starter.orm.support;

/**
 * @author cheng.wang
 * @date 2022/5/17
 */
public interface DatabaseFeature {

    String getProductName();

    String getValidationQuerySql();

    //String getPageQuerySql(String sql, MybatisPagination mybatisPagination);

    String getPageCountSql(String sql);

    String getLimitSql(String sql, int i);

    String getDateComparatorFuncSql(String dateInput);

    String getFindInSetFuncSql(String value, String columnName);
}
