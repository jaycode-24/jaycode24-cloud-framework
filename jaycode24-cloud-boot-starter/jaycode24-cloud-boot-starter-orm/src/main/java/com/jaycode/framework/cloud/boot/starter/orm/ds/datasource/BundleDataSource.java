package com.jaycode.framework.cloud.boot.starter.orm.ds.datasource;

import com.jaycode.framework.cloud.boot.starter.orm.ds.resource.DataSourceGroup;
import com.jaycode.framework.cloud.boot.starter.orm.ds.resource.DataSourceRouteManager;
import com.jaycode.framework.cloud.boot.starter.orm.ds.route.DataSourceRoutingException;

/**
 * 绑定特定路由的数据源
 * @author cheng.wang
 * @date 2022/5/16
 */
public class BundleDataSource extends LoadBalanceDataSource{

    private final String dataSourceRouteIdentity;

    public BundleDataSource(String dataSourceRouteIdentity) {
        this.dataSourceRouteIdentity = dataSourceRouteIdentity;
    }

    public String getDataSourceRouteIdentity() {
        return dataSourceRouteIdentity;
    }

    @Override
    protected DataSourceGroup routingDataSourceGroup() {
        DataSourceGroup dataSourceGroup = DataSourceRouteManager.getDataSource(dataSourceRouteIdentity);
        if (dataSourceGroup == null){
            throw new DataSourceRoutingException("无法识别数据源路由信息:" + dataSourceRouteIdentity + "，请参见配置中心是否存在该数据库路由定义");

        }
        return dataSourceGroup;
    }
}
