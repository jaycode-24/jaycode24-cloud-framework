package com.jaycode.framework.cloud.boot.starter.orm;

import com.jaycode.framework.cloud.boot.starter.orm.ds.DatabaseAspect;
import com.jaycode.framework.cloud.boot.starter.orm.ds.resource.DataSourceRouteManager;
import com.jaycode.framework.cloud.boot.starter.orm.injector.PluginInjector;
import com.jaycode.framework.cloud.boot.starter.orm.support.AutoConfiguredMapperScannerRegistrar;
import com.jaycode.framework.cloud.boot.starter.orm.support.TransactionManager;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.mybatis.spring.boot.autoconfigure.MybatisProperties;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.PlatformTransactionManager;

import javax.annotation.PostConstruct;

/**
 * ORM自动配置
 * @author cheng.wang
 * @date 2022/5/16
 */
@Configuration
@EnableConfigurationProperties(MybatisProperties.class)
@Slf4j
public class OrmAutoConfiguration implements DisposableBean {

    @Bean(name = "transactionManager")
    @Primary
    public PlatformTransactionManager regTransactionManager(){
        return TransactionManager.acquireTransactionManager();
    }

    /**
     * 导入mybatis bean 扫描器
     */
    @Configuration
    @Import(AutoConfiguredMapperScannerRegistrar.class)
    public static class MapperScannerRegistrarNotFoundConfiguration{
        public MapperScannerRegistrarNotFoundConfiguration() {
        }

        @PostConstruct
        public void afterPropertiesSet(){
            log.debug("No {} found.", MapperFactoryBean.class.getName());

        }
    }

    @Override
    public void destroy(){
        DataSourceRouteManager.destroy();
    }

    @Bean
    public PluginInjector pluginInjector() {
        return new PluginInjector();
    }

    @Bean
    @ConditionalOnClass(ProceedingJoinPoint.class)
    public DatabaseAspect databaseAspect() {
        return new DatabaseAspect();
    }
}
