package com.jaycode.framework.cloud.boot.starter.orm.ds.resource;

import com.jaycode.framework.cloud.boot.core.config.Config;
import com.jaycode.framework.cloud.boot.core.config.ConfigConst;
import com.jaycode.framework.cloud.boot.core.config.ConfigFactory;
import com.jaycode.framework.cloud.boot.starter.orm.ds.datasource.DataSourceDefinitionException;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * @author cheng.wang
 * @date 2022/5/17
 */
@Slf4j
public class DataSourceRouteConfig {

    private final static DataSourceRouteConfig _instance = new DataSourceRouteConfig();


    private final Map<String, String> dynamicRouteMap = new HashMap<>();
    private final Map<String, String> allRouteMap = new HashMap<>();
    private final String dsRouteConfigId;
    private final Config config;

    private DataSourceRouteConfig(){
        dsRouteConfigId = ConfigConst.getDsRouteRegistry();
        config = getDataSourceRouteConfig();
        if (config == null || config instanceof ConfigFactory.NotFoundConfig){
            throw new DataSourceDefinitionException("配置中心配置ID:" + dsRouteConfigId + " 不存在或配置错误");
        }
        Map<String, Object> parent = config.getValue("datasource.route");
        if (parent != null){
            parent.forEach((k,v) -> {
                if (v instanceof String){
                    allRouteMap.put(k, (String) v);
                }
                //兼容MAP格式，方便在合适的时候更新数据源配置，调整为MAP而非现在的List<Map<>>结构
                if (k.equals("dynamic") && v instanceof Map){
                    ((Map<String, String>) v).forEach(dynamicRouteMap::put);
                    ((Map<String, String>) v).forEach(allRouteMap::put);
                }
            });
        }
    }

    private Config getDataSourceRouteConfig() {
        //如果是默认读取配置
        if (dsRouteConfigId.equals(ConfigConst.DEFAULT_DS_ROUTE_REGISTRY_NAME)){
            Config config = ConfigFactory.getConfig(dsRouteConfigId);
            if (config.getValue("datasource") != null){
                log.info("通过common.yaml读取数据库路由配置");
                return config;
            }
        }
        log.info("通过{}读取数据库路由配置", dsRouteConfigId);
        return ConfigFactory.getConfig(dsRouteConfigId);
    }

    public static DataSourceRouteConfig getInstance() {
        return _instance;
    }

    public boolean hasRoute(String usingRouteId) {
        if (isDynamicRouteId(usingRouteId)) {
            return dynamicRouteMap.size() > 0;
        }
        return allRouteMap.containsKey(usingRouteId);
    }

    public boolean isDynamicRouteId(String routeId) {
        return "dynamic".equals(routeId);
    }

    public Map<String, String> getRoute(String usingRouteId) {
        if (isDynamicRouteId(usingRouteId)) {
            return dynamicRouteMap;
        }
        if (allRouteMap.containsKey(usingRouteId)) {
            Map<String, String> item = new HashMap<>();
            item.put(usingRouteId, allRouteMap.get(usingRouteId));
            return item;
        }
        return new HashMap<>();

    }

    public Map<String, Map<String, String>> getDataSourceJdbc() {
        return config.getValue("datasource.dss");
    }

    public String getDynamicRouteId() {
        return "dynamic";

    }
}
