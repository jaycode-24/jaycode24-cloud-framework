package com.jaycode.cloud.boot.starter.stream;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.stream.binder.Binder;
import org.springframework.context.annotation.Bean;

/**
 * Activemq Stream自动装配类
 * @author cheng.wang
 * @date 2022/5/19
 */
@ConditionalOnProperty(prefix = "jaycode.stream.activemq",name = "uri")
@Slf4j
public class ActiveMqStreamAutoConfiguration {
    public static final String CONFIG_PREFIX="funi.stream.activemq";

    @Value("${jaycode.stream.activemq.uri}")
    private String activemqUri;

    public ActiveMqStreamAutoConfiguration(){
        log.info("装载Activemq Stream初始化配置");
    }

    @Bean
    @ConditionalOnMissingBean(Binder.class)
    public ActiveMqMessageChannelBinder activeMqMessageChannelBinder(){
        return new ActiveMqMessageChannelBinder(activemqUri);
    }
}
