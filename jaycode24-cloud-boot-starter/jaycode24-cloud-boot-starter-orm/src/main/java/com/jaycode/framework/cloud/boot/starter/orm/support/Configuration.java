package com.jaycode.framework.cloud.boot.starter.orm.support;

/**
 * @author cheng.wang
 * @date 2022/5/17
 */
public class Configuration extends org.apache.ibatis.session.Configuration {

    /**
     * 重写获取数据库ID 设计，以便在开启仓库方言后从sql中直接获取数据库产品判断方言SQL
     *
     * @return 数据库方言
     */
    @Override
    public String getDatabaseId() {
        if (ConnectionHolder.getConnection() != null) {
            return DatabaseProduct.getFeature(ConnectionHolder.getConnection()).getProductName();
        }
        return databaseId;
    }
}
