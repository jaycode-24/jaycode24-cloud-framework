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
}
