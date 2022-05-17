package com.jaycode.framework.cloud.boot.core.config;

/**
 * @author cheng.wang
 * @date 2022/5/13
 */
public interface Config {

    boolean has(String key);

    <T> T getValue(String key);

    <T> T getValue(String key, T defaultValue);

    /**
     * 直接获取boolean 类型值，如果配置值为null则返回false
     *
     * @param key 配置项键名
     * @return 配置项值
     */
    Boolean getBoolean(String key);

    String asString();

    void addListener(ConfigListener configListener);

    /**
     * 获取配置所在命名空间
     *
     * @return 配置命名空间
     */
    static String getNamespace() {
        return System.getProperty("spring.cloud.nacos.discovery.namespace");
    }
}
