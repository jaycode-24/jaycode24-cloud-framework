package com.jaycode.framework.cloud.boot.core;

import com.jaycode.framework.cloud.boot.core.support.BootstrapStream;
import org.springframework.boot.autoconfigure.AutoConfigurationExcludeFilter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.TypeExcludeFilter;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.type.filter.TypeFilter;

/**
 * 启动类
 * 完成配置初始化，环境便令设置等欲启动信息，同时限定bean扫描范围为com.jaycode
 * @author cheng.wang
 * @date 2022/5/13
 */

/**
 * 内有
 * @Import(AutoConfigurationImportSelector.class) 通过getCandidateConfigurations方法
 * 调用spring的SpringFactoriesLoader.loadFactoryNames
 * 会加载META-INF/spring.factories 下的xxAutoConfiguration
 */
@EnableAutoConfiguration(excludeName = {
        "org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration",
        "org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration",
        "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration",
        "org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration",
        "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration",
        "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration",
        "org.redisson.spring.starter.RedissonAutoConfiguration",
        //取消hibernateJpa自动装载，避免产生方言问题
        "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration",
        //取消Nacos自动注册改为通过funi.standone依赖
        "org.springframework.cloud.alibaba.nacos.NacosConfigAutoConfiguration"
})
/**
 * bean扫描规则
 * 1、基于基础包名com.funi
 * 2、过滤{过滤规则自定义}
 */
@ComponentScan(basePackages = "com.jaycode",
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.CUSTOM, classes = TypeFilter.class),
                @ComponentScan.Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class),
                @ComponentScan.Filter(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class)
        })
@EnableDiscoveryClient
public class JayCodeApplication {

    /**
     * 微服务启动方法
     * @param cls   启动类
     * @param args  参数
     */
    public static void run(Class<?> cls, String... args){
        BootstrapStream.
                of(args).//配置日志路径
                configNacos().
                optimizeApplicationBootClass().
                start(cls);

    }

    /**
     * 基于WAR部署，则直接配置Application即可
     *
     * @param builder 构建器
     * @return 构建器
     */
    public static SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        BootstrapStream.of(null).configNacos()
                .optimizeApplicationBootClass();
        return builder.sources(builder.application().getMainApplicationClass(), JayCodeApplication.class);
    }
}
