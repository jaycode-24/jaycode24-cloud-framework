package com.jaycode.framework.cloud.boot.starter.orm.ds.resource;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * 数据源路由管理器
 *  * 负责从配置中心的配置文件加载，并初始化相关数据库连接
 * @author cheng.wang
 * @date 2022/5/16
 */
@Slf4j
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
        if (ROUTE_CONFIG.hasRoute(databaseRouteId) && !DATA_SOURCE_GROUP_CACHE.containsKey(databaseRouteId)) {
            Set<String> set = new HashSet<>();
            set.add(databaseRouteId);
            init(set);
        }
    }

    private static void init(Set<String> appRequireRouteIds) {
        log.info("初始化数据库路由[" + StringUtils.join(appRequireRouteIds, DEFAULT_SPLIT) + "]");
        Map<String,String> appRequireRouteIdConfig = new HashMap<>();
        for (String usingRouteId : appRequireRouteIds) {
            if (ROUTE_CONFIG.hasRoute(usingRouteId)){
                ROUTE_CONFIG.getRoute(usingRouteId)
                        .forEach(appRequireRouteIdConfig::put);

            }
        }
        ROUTE_CONFIG.
    }
}
