package com.jaycode.framework.cloud.boot.core.config;

/**
 * 常量表
 * @author cheng.wang
 * @date 2022/5/13
 */
public class ConfigConst {

    //配置中心地址
    public static final String CONFIG_SERVER_DEF_KEY = "funi.cloud.config.server";
    public static final String SHARE_DATA_ID = "common.yaml";
    //应用自定义的路由ID配置键名
    public static final String LOCAL_STATIC_BUNDLE_DB_LINK_CONFIG_KEY = "funi.cloud.config.ds.route";
    private static final BootstrapEnvironment config;
    //用户自定义路由表配置名
    public static final String CUSTOM_DS_ROUTE_REGISTRY_KEY = "funi.cloud.config.ds.source";
    //默认数据路由配置文件名
    public static final String DEFAULT_DS_ROUTE_REGISTRY_NAME = "common.yaml";
    //用户自定义最大分页大小
    public static final String CUSTOM_MAX_PAGE_SIZE_KEY = "funi.cloud.config.maxPageSize";
    //默认最大分页大小
    private static final Integer DEFAULT_MAX_PAGE_SIZE = 100;
    static {
        config = new BootstrapEnvironment();
    }
    public static String getApplicationName(){
        return config.getString("spring.application.name");
    }

    public static Boolean isJtaEnabled() {
        return config.getBoolean("spring.jta.enabled");
    }

    public static String getDefaultRouteId() {
        return config.getString(LOCAL_STATIC_BUNDLE_DB_LINK_CONFIG_KEY);
    }

    public static String getDsRouteRegistry() {
        return config.getString(CUSTOM_DS_ROUTE_REGISTRY_KEY, DEFAULT_DS_ROUTE_REGISTRY_NAME);
    }

    public static Integer getMaxPageSize() {
        return config.getInteger(CUSTOM_MAX_PAGE_SIZE_KEY, DEFAULT_MAX_PAGE_SIZE);

    }
}
