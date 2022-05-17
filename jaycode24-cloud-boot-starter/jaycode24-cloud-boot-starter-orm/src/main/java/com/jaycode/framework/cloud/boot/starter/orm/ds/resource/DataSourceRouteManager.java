package com.jaycode.framework.cloud.boot.starter.orm.ds.resource;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * 数据源路由管理器
 *  * 负责从配置中心的配置文件加载，并初始化相关数据库连接
 * @author cheng.wang
 * @date 2022/5/16
 */
public class DataSourceRouteManager {
    //数据源路由缓存
    private static final Map<String, DataSourceGroup> DATA_SOURCE_GROUP_CACHE = new HashMap<>();
    private static final DataSourceRouteConfig ROUTE_CONFIG = DataSourceRouteConfig.getInstance();
    private static final String DEFAULT_SPLIT = ",";


    public static DataSourceGroup getDataSource(String dataSourceIdentity) {
        return DATA_SOURCE_GROUP_CACHE.get(dataSourceIdentity);

    }

    public static void destroy() {
        for (DataSourceGroup dataSourceGroup : DATA_SOURCE_GROUP_CACHE.values()) {
            dataSourceGroup.close();
        }
    }

    /**
     * 根据路由初始化数据库连接，如果路由定义存在
     *
     * @param databaseRouteId 路由标识
     */
    public static void initIfNecessary(String databaseRouteId) {
        if ()
    }
}
