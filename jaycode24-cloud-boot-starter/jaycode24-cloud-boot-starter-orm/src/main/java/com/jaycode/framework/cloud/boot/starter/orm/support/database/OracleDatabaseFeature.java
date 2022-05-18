package com.jaycode.framework.cloud.boot.starter.orm.support.database;

import com.jaycode.framework.cloud.boot.starter.orm.page.MybatisPagination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.InvalidDataAccessResourceUsageException;

public class OracleDatabaseFeature extends BaseDatabaseFeature {
    private static final Logger logger = LoggerFactory.getLogger(OracleDatabaseFeature.class);

    @Override
    public String getValidationQuerySql() {
        return "select 1 from dual";
    }

    @Override
    public String getProductName() {
        return "oracle";
    }

    /**
     * 修改记录
     * 1 第一次文件兼容分页排序 在原有语句上增加了一层 查询，但是直接导致排序错误
     * 2 还原到最初的分页语句，但是要求有分页的排序必须包含唯一主键
     */
    /*@Override
    public String getPageQuerySql(String sql, MybatisPagination page) {
        if (!sql.contains("order by ")) {
            if (logger.isWarnEnabled()) {
                logger.warn("in oracle database paging query , the sql should contains primary key sort ");
            }
        }
        return "select * from ( select tmp_page.*, rownum row_id from ( " + sql
                + " ) tmp_page where rownum <= " + page.getEndRow() + " ) where row_id > " + page.getStartRow();
    }*/

    @Override
    public String getPageCountSql(String sql) {
        return "select count(*) from (" + sql + ") tmp_count";
    }

    @Override
    public String getLimitSql(String sql, int i) {
        if (sql.contains("where")) {
            if (sql.contains("order by")) {
                int orderByStartIndex = sql.indexOf("order by");
                return sql.substring(0, orderByStartIndex) + "and rownum <=" + i + " " + sql.substring(orderByStartIndex, sql.length());
            }
            return sql + " and rownum <=" + i;
        } else {
            if (sql.contains("order by")) {
                int orderByStartIndex = sql.indexOf("order by");
                return sql.substring(0, orderByStartIndex) + "where rownum <=" + i + " " + sql.substring(orderByStartIndex, sql.length());
            }
            return sql + " where rownum<=" + i;
        }
    }

    @Override
    public String getDateComparatorFuncSql(String dateInput) {
        return String.format("to_date(%s,'yyyy-mm-dd hh24:mi:ss')", dateInput);
    }

    /**
     * 采用 =|a,|,a|,a, 模式匹配，该方法只能保证功能使用无法保证SQL注入安全，由上层应用保证SQL注入安全
     */
    @Override
    public String getFindInSetFuncSql(String value, String columnName) {
        if (value.contains("'") || value.contains("\"")) {
            throw new InvalidDataAccessResourceUsageException("Invalid find_in_set value");
        }

        return "(C='V' or C  like 'V,%' or C  like '%,V' or C  like '%,V,%' )".replaceAll("C", columnName).replaceAll("V", value);
    }
}
