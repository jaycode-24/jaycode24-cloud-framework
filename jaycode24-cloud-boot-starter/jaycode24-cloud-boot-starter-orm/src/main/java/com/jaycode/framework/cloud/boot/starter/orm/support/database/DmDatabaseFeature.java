package com.jaycode.framework.cloud.boot.starter.orm.support.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DmDatabaseFeature extends OracleDatabaseFeature {
    private static final Logger logger = LoggerFactory.getLogger(DmDatabaseFeature.class);


    @Override
    public String getProductName() {
        return "dm";
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
}
