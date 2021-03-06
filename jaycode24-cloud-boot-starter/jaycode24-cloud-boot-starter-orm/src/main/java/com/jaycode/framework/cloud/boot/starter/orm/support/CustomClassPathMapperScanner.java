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
     * ?????????bean??????????????????????????????@Repository??????bean
     *
     * @param basePackages
     * @return
     */
    @Override
    public Set<BeanDefinitionHolder> doScan(String... basePackages) {
        Set<BeanDefinitionHolder> definitionHolderSet = super.doScan(basePackages);
        //???????????????????????????
        Set<String> usingDataRouteIds = new HashSet<>();

        definitionHolderSet.forEach(h -> {
            MutablePropertyValues mutablePropertyValues = h.getBeanDefinition().getPropertyValues();
            try {
                boolean dataSourceBundled = false;
                AnnotationMetadata annotationMetadata = ((ScannedGenericBeanDefinition) h.getBeanDefinition()).getMetadata();
                if (annotationMetadata.hasAnnotation(Repository.class.getName())) {
                    String repositoryName = ((ScannedGenericBeanDefinition) h.getBeanDefinition()).getMetadata().getClassName();
                    Map<String, Object> props = annotationMetadata.getAnnotationAttributes(Repository.class.getName());
                    //??????configuration???????????????????????????????????????????????????dataSource???????????????createSqlSessionFactory ????????????dataSource????????????configuration?????????
                    Configuration configuration = createMybatisConfig(com.jaycode.framework.cloud.boot.starter.orm.support.Configuration.class);
                    if (props != null) {
                        if (props.containsKey(REPOSITORY_ATTRIBUTE_MULTI_DIALECT) && ((Boolean) props.getOrDefault(REPOSITORY_ATTRIBUTE_MULTI_DIALECT, false))) {
                            log.debug(repositoryName + "??????SQL????????????");
                            configuration = createMybatisConfig(DialectConfiguration.class);

                        }
                        configuration.setMapUnderscoreToCamelCase(true);

                        //????????????Mybatis???????????????CRUD??????
                        templateSqlInjector.inject(configuration,Class.forName(repositoryName));
                        if (props.containsKey(REPOSITORY_ATTRIBUTE_ROUTE) && StringUtils.hasText(props.get(REPOSITORY_ATTRIBUTE_ROUTE).toString())) {
                            String route = props.get(REPOSITORY_ATTRIBUTE_ROUTE).toString();
                            mutablePropertyValues.add(MAPPER_BEAN_NAME,
                                    createSqlSessionFactory(dataSourceFactory.prepareNewDataSource(route), configuration));
                            dataSourceBundled = true;
                            usingDataRouteIds.add(route);
                            REPOSITORY_CACHE.addRepoDataSourceRouteId(repositoryName, route);
                            log.debug("????????????{}????????????????????????{}??????", h.getBeanName(), route);
                        }
                    }
                    if (!dataSourceBundled) {
                        BundleDataSource dataSource = (BundleDataSource) dataSourceFactory.prepareDefaultDataSource();
                        //????????????????????????????????????????????????
                        if (dataSource != null) {
                            mutablePropertyValues.add(MAPPER_BEAN_NAME,
                                    createSqlSessionFactory(dataSource, configuration));
                            log.debug("????????????{}???????????????????????????????????????", h.getBeanName());
                            usingDataRouteIds.add(dataSource.getDataSourceRouteIdentity());
                            REPOSITORY_CACHE.addRepoDataSourceRouteId(repositoryName, dataSource.getDataSourceRouteIdentity());
                        } else {
                            log.debug("????????????{}??????????????????????????????????????????", h.getBeanName());
                            mutablePropertyValues.add(MAPPER_BEAN_NAME,
                                    createSqlSessionFactory(dataSourceFactory.prepareDynamicDataSource(), configuration));
                            usingDataRouteIds.add(DATA_SOURCE_ROUTE_CONFIG.getDynamicRouteId());
                            REPOSITORY_CACHE.addRepoDataSourceRouteId(repositoryName, DATA_SOURCE_ROUTE_CONFIG.getDynamicRouteId());
                        }
                    }
                }else if (annotationMetadata.hasAnnotation(org.springframework.stereotype.Repository.class.getName())) {
                    throw new RepositoryCreateException("????????????" + h.getBeanName() + "?????????" + Repository.class.getName() + "??????");
                }
            } catch (Exception e) {
                throw new RepositoryScanException(e.getMessage(), e);
            }
        });
        //??????????????????????????????????????????jta
        //todo ????????????????????????jta ??????????????????
       /* if (usingDataRouteIds.size() > 1) {
            //
            //Assert.isTrue(TransactionManager.usingJta(), "??????????????????????????????[" + org.apache.commons.lang3.StringUtils.join(usingDataRouteIds, ",") + "]???????????????????????????");
        }*/
        //??????????????????????????????????????????????????????ds,?????????????????????????????????repo??????
        if (usingDataRouteIds.size() == 0) {
            log.debug("?????????????????????????????????????????????????????????????????????????????????????????????");
            BundleDataSource bundleDataSource = (BundleDataSource) dataSourceFactory.prepareDefaultDataSource();
            if (bundleDataSource != null) {
                log.debug("???????????????????????????:{}", bundleDataSource.getDataSourceRouteIdentity());
                usingDataRouteIds.add(bundleDataSource.getDataSourceRouteIdentity());
            } else {
                log.debug("???????????????????????????");
            }
        }
        if (usingDataRouteIds.size() > 0) {
            dataSourceFactory.init(usingDataRouteIds);
        }
        return definitionHolderSet;
    }


    private Configuration createMybatisConfig(Class<? extends Configuration> configurationClass) throws Exception {
        Configuration configuration = configurationClass.newInstance();
        //??????null??????JDBC??????
        configuration.setJdbcTypeForNull(JdbcType.NULL);
        //??????mybatis?????????????????????mybatis????????????????????????
        configuration.setCacheEnabled(false);
        return configuration;
    }

    /**
     * ??????SqlSessionFactory ??????
     * @param dataSource
     * @param configuration
     * @return
     * @throws Exception
     */
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
