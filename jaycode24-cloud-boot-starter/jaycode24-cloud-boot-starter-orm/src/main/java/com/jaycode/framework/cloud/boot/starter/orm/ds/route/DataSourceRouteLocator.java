package com.jaycode.framework.cloud.boot.starter.orm.ds.route;

/**
 * 数据源路由定位器，定位数据源路由
 * @author cheng.wang
 * @date 2022/5/16
 */
public class DataSourceRouteLocator {

    private static final ThreadLocal<DataSourceRoute> DS_LOCATOR = new ThreadLocal<>();

    public static void setRoute(DataSourceRoute dataSourceRoute) {
        DS_LOCATOR.set(dataSourceRoute);
    }

    public static DataSourceRoute getRoute() {
        return DS_LOCATOR.get();
    }

    public static void clean() {
        DS_LOCATOR.remove();
    }

}
