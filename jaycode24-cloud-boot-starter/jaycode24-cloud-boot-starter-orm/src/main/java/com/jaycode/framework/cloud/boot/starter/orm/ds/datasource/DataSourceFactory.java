package com.jaycode.framework.cloud.boot.starter.orm.ds.datasource;

import cn.hutool.core.util.StrUtil;
import com.jaycode.framework.cloud.boot.core.config.ConfigConst;
import com.jaycode.framework.cloud.boot.starter.orm.ds.resource.DataSourceGroup;
import com.jaycode.framework.cloud.boot.starter.orm.ds.resource.DataSourceRouteManager;
import com.jaycode.framework.cloud.boot.starter.orm.ds.route.DataSourceRoute;
import com.jaycode.framework.cloud.boot.starter.orm.ds.route.DataSourceRouteLocator;
import com.jaycode.framework.cloud.boot.starter.orm.ds.route.DataSourceRoutingException;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;

/**
 * 数据源工厂，产生特定的数据源
 * 包括 绑定类、动态类、默认类
 * @author cheng.wang
 * @date 2022/5/16
 */
@Slf4j
public class DataSourceFactory {

    private static final DataSourceFactory INSTANCE = new DataSourceFactory();

    /**
     * 动态数据源实例，去jta的情况下，为保证事务的一致性，需设计为单例对象
     */
    private final DataSource DYNAMIC = new DynamicDataSource();

    public static DataSourceFactory getInstance() {
        return INSTANCE;
    }

    public DataSource prepareDefaultDataSource() {
        return DataSourceHolder.instance;
    }

    public DataSource prepareDynamicDataSource() {
        return DYNAMIC;
    }

    private static class DataSourceHolder{
        private static DataSource instance = null;

        static {
            String defaultRoute = ConfigConst.getDefaultRouteId();
            if (StrUtil.isNotBlank(defaultRoute)){
                log.info("检测到数据库默认配置信息" + defaultRoute);
                instance = new BundleDataSource(defaultRoute);
            }else {
                log.info("未检测到数据库默认配置");
            }
        }
    }


    private class DynamicDataSource extends LoadBalanceDataSource {
        private DynamicDataSource(){}

        @Override
        protected DataSourceGroup routingDataSourceGroup() {
            DataSourceRoute dataSourceRoute = DataSourceRouteLocator.getRoute();
            DataSourceGroup dataSourceGroup = dataSourceRoute == null ? null :
                    DataSourceRouteManager.getDataSource(dataSourceRoute.getId());
            if (dataSourceGroup == null ){
                throw new DataSourceRoutingException("未定义数据源路由信息[" + (dataSourceRoute != null ? dataSourceRoute.getId() : "") + "]");

            }else {
                return dataSourceGroup;
            }
        }
    }
}
