package com.jaycode.framework.cloud.boot.starter.orm.support;

import com.jaycode.framework.cloud.boot.starter.orm.ds.RepositoryScanException;
import com.jaycode.framework.cloud.boot.starter.orm.ds.annotation.Repository;
import com.jaycode.framework.cloud.boot.starter.orm.ds.datasource.BundleDataSource;
import com.jaycode.framework.cloud.boot.starter.orm.ds.datasource.DataSourceFactory;
import com.jaycode.framework.cloud.boot.starter.orm.ds.resource.DataSourceRouteConfig;
import com.jaycode.framework.cloud.boot.starter.orm.injector.PluginInjector;
import com.jaycode.framework.cloud.boot.starter.orm.injector.RepositoryCache;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.JdbcType;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.boot.autoconfigure.MybatisProperties;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;
import org.mybatis.spring.mapper.ClassPathMapperScanner;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author cheng.wang
 * @date 2022/5/16
 */
@Slf4j
public class CustomClassPathMapperScanner extends ClassPathMapperScanner {

    private static final String REPOSITORY_ATTRIBUTE_MULTI_DIALECT = "dialect";
    private static final String REPOSITORY_ATTRIBUTE_ROUTE = "route";
    private static final String MAPPER_BEAN_NAME = "sqlSessionFactory";
    private static final DataSourceFactory dataSourceFactory = DataSourceFactory.getInstance();
    private static final RepositoryCache REPOSITORY_CACHE = RepositoryCache.getInstance();
    private static final DataSourceRouteConfig DATA_SOURCE_ROUTE_CONFIG = DataSourceRouteConfig.getInstance();


    private PluginInjector templateSqlInjector = new PluginInjector();

    public CustomClassPathMapperScanner(BeanDefinitionRegistry registry) {
        super(registry);
        setAnnotationClass(Repository.class);
    }


    /**
     * 自定义bean扫描，主要扫描自定义@Repository注解bean
     *
     * @param basePackages
     * @return
     */
    @Override
    public Set<BeanDefinitionHolder> doScan(String... basePackages) {
        Set<BeanDefinitionHolder> definitionHolderSet = super.doScan(basePackages);
        //收集使用到的数据源
        Set<String> usingDataRouteIds = new HashSet<>();

        definitionHolderSet.forEach(h -> {
            MutablePropertyValues mutablePropertyValues = h.getBeanDefinition().getPropertyValues();
            try {
                boolean dataSourceBundled = false;
                AnnotationMetadata annotationMetadata = ((ScannedGenericBeanDefinition) h.getBeanDefinition()).getMetadata();
                if (annotationMetadata.hasAnnotation(Repository.class.getName())) {
                    String repositoryName = ((ScannedGenericBeanDefinition) h.getBeanDefinition()).getMetadata().getClassName();
                    Map<String, Object> props = annotationMetadata.getAnnotationAttributes(Repository.class.getName());
                    //此处configuration一定要新建示例，否则会造成仓库对象dataSource混用，因为createSqlSessionFactory 实际上把dataSource还是放在configuration对象中
                    Configuration configuration = createMybatisConfig(com.jaycode.framework.cloud.boot.starter.orm.support.Configuration.class);
                    if (props != null) {
                        if (props.containsKey(REPOSITORY_ATTRIBUTE_MULTI_DIALECT) && ((Boolean) props.getOrDefault(REPOSITORY_ATTRIBUTE_MULTI_DIALECT, false))) {
                            log.debug(repositoryName + "启用SQL方言支持");
                            configuration = createMybatisConfig(DialectConfiguration.class);

                        }
                        configuration.setMapUnderscoreToCamelCase(true);

                        //自动注入Mybatis插件，开启CRUD功能
                        templateSqlInjector.inject(configuration,Class.forName(repositoryName));
                        if (props.containsKey(REPOSITORY_ATTRIBUTE_ROUTE) && StringUtils.hasText(props.get(REPOSITORY_ATTRIBUTE_ROUTE).toString())) {
                            String route = props.get(REPOSITORY_ATTRIBUTE_ROUTE).toString();
                            mutablePropertyValues.add(MAPPER_BEAN_NAME,
                                    createSqlSessionFactory(dataSourceFactory.prepareNewDataSource(route), configuration));
                            dataSourceBundled = true;
                            usingDataRouteIds.add(route);
                            REPOSITORY_CACHE.addRepoDataSourceRouteId(repositoryName, route);
                            log.debug("实体仓库{}数据源自动绑定到{}路由", h.getBeanName(), route);
                        }
                    }
                    if (!dataSourceBundled) {
                        BundleDataSource dataSource = (BundleDataSource) dataSourceFactory.prepareDefaultDataSource();
                        //默认数据源路由和动态路由是互斥的
                        if (dataSource != null) {
                            mutablePropertyValues.add(MAPPER_BEAN_NAME,
                                    createSqlSessionFactory(dataSource, configuration));
                            log.debug("实体仓库{}，数据源自动绑定到默认路由", h.getBeanName());
                            usingDataRouteIds.add(dataSource.getDataSourceRouteIdentity());
                            REPOSITORY_CACHE.addRepoDataSourceRouteId(repositoryName, dataSource.getDataSourceRouteIdentity());
                        } else {
                            log.debug("实体仓库{}数据源自动绑定到动态路由资源", h.getBeanName());
                            mutablePropertyValues.add(MAPPER_BEAN_NAME,
                                    createSqlSessionFactory(dataSourceFactory.prepareDynamicDataSource(), configuration));
                            usingDataRouteIds.add(DATA_SOURCE_ROUTE_CONFIG.getDynamicRouteId());
                            REPOSITORY_CACHE.addRepoDataSourceRouteId(repositoryName, DATA_SOURCE_ROUTE_CONFIG.getDynamicRouteId());
                        }
                    }
                }else if (annotationMetadata.hasAnnotation(org.springframework.stereotype.Repository.class.getName())) {
                    throw new RepositoryCreateException("实体仓库" + h.getBeanName() + "请使用" + Repository.class.getName() + "注解");
                }
            } catch (Exception e) {
                throw new RepositoryScanException(e.getMessage(), e);
            }
        });
        //如果存在多个数据源则，应使用jta
        //todo 取消多个数据源的jta 事物强制要求
       /* if (usingDataRouteIds.size() > 1) {
            //
            //Assert.isTrue(TransactionManager.usingJta(), "检测到多个数据源路由[" + org.apache.commons.lang3.StringUtils.join(usingDataRouteIds, ",") + "]，请使用分布式事务");
        }*/
        //如果为检测到数据源，但是有定义了默认ds,初始化默认路由，方便非repo架构
        if (usingDataRouteIds.size() == 0) {
            log.debug("通过仓库扫描，未产生找到任何数据源路由定义，尝试初始化默认路由");
            BundleDataSource bundleDataSource = (BundleDataSource) dataSourceFactory.prepareDefaultDataSource();
            if (bundleDataSource != null) {
                log.debug("默认路由初始化成功:{}", bundleDataSource.getDataSourceRouteIdentity());
                usingDataRouteIds.add(bundleDataSource.getDataSourceRouteIdentity());
            } else {
                log.debug("默认路由初始化失败");
            }
        }
        if (usingDataRouteIds.size() > 0) {
            dataSourceFactory.init(usingDataRouteIds);
        }
        return definitionHolderSet;
    }


    private Configuration createMybatisConfig(Class<? extends Configuration> configurationClass) throws Exception {
        Configuration configuration = configurationClass.newInstance();
        //支持null设置JDBC类型
        configuration.setJdbcTypeForNull(JdbcType.NULL);
        //关闭mybatis二级缓存，注意mybatis默认开启一级缓存
        configuration.setCacheEnabled(false);
        return configuration;
    }

    public SqlSessionFactory createSqlSessionFactory(DataSource dataSource, Configuration configuration) throws Exception {
        SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
        factory.setDataSource(dataSource);
        factory.setVfs(SpringBootVFS.class);
        MybatisProperties properties = new MybatisProperties();
        properties.setMapperLocations(new String[]{"classpath*:/repository/**/*Repository.xml"});
        factory.setConfiguration(configuration);
        factory.setConfigurationProperties(properties.getConfigurationProperties());
        if (!ObjectUtils.isEmpty(properties.resolveMapperLocations())) {
            factory.setMapperLocations(properties.resolveMapperLocations());
        }
        return factory.getObject();
    }
}
