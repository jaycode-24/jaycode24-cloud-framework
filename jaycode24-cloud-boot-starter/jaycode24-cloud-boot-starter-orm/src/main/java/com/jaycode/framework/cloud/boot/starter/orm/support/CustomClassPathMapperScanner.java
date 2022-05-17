package com.jaycode.framework.cloud.boot.starter.orm.support;

import com.jaycode.framework.cloud.boot.starter.orm.ds.RepositoryScanException;
import com.jaycode.framework.cloud.boot.starter.orm.ds.annotation.Repository;
import com.jaycode.framework.cloud.boot.starter.orm.injector.PluginInjector;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.JdbcType;
import org.mybatis.spring.mapper.ClassPathMapperScanner;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;
import java.util.Set;

/**
 * @author cheng.wang
 * @date 2022/5/16
 */
@Slf4j
public class CustomClassPathMapperScanner extends ClassPathMapperScanner {

    private static final String REPOSITORY_ATTRIBUTE_MULTI_DIALECT = "dialect";
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
                    }
                }
            } catch (Exception e) {
                throw new RepositoryScanException(e.getMessage(), e);
            }
        });
    }


    private Configuration createMybatisConfig(Class<? extends Configuration> configurationClass) throws Exception {
        Configuration configuration = configurationClass.newInstance();
        //支持null设置JDBC类型
        configuration.setJdbcTypeForNull(JdbcType.NULL);
        //关闭mybatis二级缓存，注意mybatis默认开启一级缓存
        configuration.setCacheEnabled(false);
        return configuration;
    }
}
