package com.jaycode.framework.cloud.boot.starter.orm.ds.resource;

import cn.hutool.core.util.StrUtil;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.jaycode.framework.cloud.boot.core.config.ConfigConst;
import com.jaycode.framework.cloud.boot.core.support.connection.ConnectionUtils;
import com.jaycode.framework.cloud.boot.starter.orm.ds.datasource.DataSourceDefinitionException;
import com.jaycode.framework.cloud.boot.starter.orm.support.DatabaseFeature;
import com.jaycode.framework.cloud.boot.starter.orm.support.DatabaseProduct;
import com.jaycode.framework.cloud.boot.starter.orm.support.TransactionManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.jta.atomikos.AtomikosDataSourceBean;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.util.*;

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

    public static void init(Set<String> appRequireRouteIds) {
        log.info("初始化数据库路由[" + StringUtils.join(appRequireRouteIds, DEFAULT_SPLIT) + "]");
        Map<String,String> appRequireRouteIdConfig = new HashMap<>();
        for (String usingRouteId : appRequireRouteIds) {
            if (ROUTE_CONFIG.hasRoute(usingRouteId)){
                ROUTE_CONFIG.getRoute(usingRouteId)
                        .forEach(appRequireRouteIdConfig::put);

            }
        }
        Map<String, Map<String, String>> dss = ROUTE_CONFIG.getDataSourceJdbc();
        appRequireRouteIdConfig.forEach((k, dataSourceIds) -> {
            //增加重复性判断，支持数据库连接池重载
            if (!DATA_SOURCE_GROUP_CACHE.containsKey(k)){
                DataSourceGroup dataSourceGroup = new DataSourceGroup();
                DATA_SOURCE_GROUP_CACHE.put(k,dataSourceGroup);
                Arrays.asList(dataSourceIds.split(DEFAULT_SPLIT)).forEach(item -> {
                    Map<String, String> dssDef = dss.get(item);
                    if (dssDef == null){
                        throw new DataSourceDefinitionException("未找到连接名" + item + "对应的连接配置信息，请检查datasource.dss配置");
                    }
                    try {
                        DataSource dataSource;
                        //todo 没明白什么意思
                        if (dssDef.containsKey("jndi-name")){
                            String jndiName = dssDef.get("jndi-name");
                            Context context = new InitialContext();
                            //jdbc/hsip
                            dataSource = (DataSource) context.lookup("java:comp/env/" + jndiName);
                        }else {
                            //根据数据源连接初始化
                            String jdbc = dssDef.get("url");
                            DatabaseFeature databaseFeature = DatabaseProduct.getFeature(jdbc);
                            if (StringUtils.isEmpty(dataSourceGroup.getDialect())) {
                                dataSourceGroup.setDialect(databaseFeature.getProductName());
                            }
                            Properties properties = new Properties();
                            if (jdbc.contains("?")) {
                                String jdbcConnectionProperties = jdbc.split("\\?")[1];
                                properties.put(DruidDataSourceFactory.PROP_CONNECTIONPROPERTIES, jdbcConnectionProperties.replace("&", ";"));
                            }
                            for (Map.Entry<String, String> p : dssDef.entrySet()) {
                                String value = p.getValue();
                                if (p.getKey().equals("password")) {
                                    value = ConnectionUtils.decode(value);
                                }
                                properties.setProperty(p.getKey(), value);
                            }
                            reloadFromJavaSystemProperties(item, properties);
                            log.info("准备初始化数据库路由:" + k);
                            dataSource = createDataSource(k, databaseFeature, properties);
                            log.info("数据库路由:" + k + "初始化完成");
                        }
                        dataSourceGroup.add(item,dataSource);
                    }catch (Exception e){
                        log.error("数据库路由:" + k + "初始化出错");
                        throw new DataSourceDefinitionException(e.getMessage(), e);
                    }
                });
            }
        });




    }



    private static void reloadFromJavaSystemProperties(String dsId, Properties properties) {
        properties.stringPropertyNames().forEach((r) -> {
            String exp = System.getProperty("datasource." + dsId + "." + r);
            if (StrUtil.isNotBlank(exp)) {
                log.info("数据源[" + dsId + "]配置" + r + "已通过Java属性重写，当前最新值为:" + exp);
                properties.setProperty(r, exp);
            }
        });
    }



    /**
     * 创建数据源，分为两种
     * A：DRUID 数据源
     * initialSize 启动程序时，在连接池中初始化多少个连接
     * maxActive 连接池中最多支持多少个活动会话
     * maxWait 程序向连接池中请求连接时,超过maxWait的值后，认为本次请求失败，即连接池没有可用连接，单位毫秒，设置-1时表示无限等待
     * minEvictableIdleTimeMillis 池中某个连接的空闲时长达到 N 毫秒后, 连接池在下次检查空闲连接时，将回收该连接,要小于防火墙超时设置
     * timeBetweenEvictionRunsMillis 检查空闲连接的频率，单位毫秒, 非正整数时表示不进行检查
     * minIdle 回收空闲连接时，将保证至少有minIdle个连接，一般与initialSize相同
     * removeAbandoned 要求程序从池中get到连接后, N 秒后必须close,否则druid 会强制回收该连接,不管该连接中是活动还是空闲, 以防止进程不会进行close而霸占连接。
     * removeAbandonedTimeout 设置druid 强制回收连接的时限，当程序从池中get到连接开始算起，超过此值后，druid将强制回收该连接，单位秒。
     * testWhileIdle 当程序请求连接，池在分配连接时，是否先检查该连接是否有效。(高效)
     * validationQuery 检查池中的连接是否仍可用的 SQL 语句,drui会连接到数据库执行该SQL
     * <p>
     * <p>
     * B：Atomikos 数据源
     * min-pool-size 最小连接数
     * max-pool-size 最大连接数
     * max-lifetime 连接最大存活时间
     * borrow-connection-timeout 获取连接失败重新获等待最大时间，在这个时间内如果有可用连接，将返回
     * login-timeout java数据库连接池，最大可等待获取datasource的时间
     * maintenance-interval 连接回收时间
     * max-idle-time 最大闲置时间，超过最小连接池连接的连接将将关闭
     * test-query 测试SQL
     *
     * @param k               数据源路由名称
     * @param databaseFeature 数据库特性
     * @param properties      数据库连接配置
     * @return 数据源
     * @throws Exception
     */
    private static DataSource createDataSource(String k, DatabaseFeature databaseFeature, Properties properties) throws Exception {
        DataSource dataSource;
        if (TransactionManager.usingJta()) {
            AtomikosDataSourceBean aDataSource = new AtomikosDataSourceBean();
            //resource id 增加应用名称，避免可能的冲突
            aDataSource.setBeanName("XA-DS-"
                    + ConfigConst.getApplicationName().toUpperCase() + ObjectId.getGeneratedMachineIdentifier() + StringUtils.upperCase(k));
            aDataSource.setXaDataSourceClassName(com.alibaba.druid.pool.xa.DruidXADataSource.class.getName());
            aDataSource.setMinPoolSize(Integer.parseInt(properties.getProperty("minIdle", "4")));
            aDataSource.setMaxPoolSize(Integer.parseInt(properties.getProperty("maxActive", (Runtime.getRuntime().availableProcessors() * 2 + 1) + "")));
            aDataSource.setBorrowConnectionTimeout(Integer.parseInt(properties.getProperty("borrowConnectionTimeout", "60")));
            //此配置尝试解决 wait_timeout
            //获取等待时间
            aDataSource.setLoginTimeout(Long.valueOf(properties.getProperty("maxWait", "60")).intValue());
            aDataSource.setMaintenanceInterval(Integer.parseInt(properties.getProperty("maintenanceInterval", "60")));
            aDataSource.setMaxIdleTime(Integer.parseInt(properties.getProperty("maxIdle", "60")));
            aDataSource.setTestQuery(properties.getProperty("validationQuery", databaseFeature.getValidationQuerySql()));
            //maxLifetime 连接存活时间
            aDataSource.setMaxLifetime(Integer.parseInt(properties.getProperty("minEvictableIdleTimeMillis", "20000000")) / 1000);
            aDataSource.setXaProperties(properties);
            aDataSource.afterPropertiesSet();
            dataSource = aDataSource;
        } else {
            dataSource = DruidDataSourceFactory.createDataSource(properties);
        }
        return dataSource;
    }
}
