package com.jaycode.cloud.boot.starter.stream;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.stream.binder.Binder;
import org.springframework.context.annotation.Bean;

/**
 * @author cheng.wang
 * @date 2022/5/19
 */
@ConditionalOnProperty(prefix = "jaycode.activemq", name = "uri")
@ConditionalOnMissingBean(ActiveMqMessageChannelBinder.class)
@Slf4j
public class ActiveMqStreamOldAutoConfiguration {
    public static final String CONFIG_PREFIX = "funi.activemq";

    @Value("${jaycode.activemq.uri}")
    private String activemqUri;

    public ActiveMqStreamOldAutoConfiguration(){
        log.info("装载Activemq Stream初始化配置");

    }

    @Bean
    @ConditionalOnMissingBean(Binder.class)
    public ActiveMqMessageChannelBinder activeMqMessageChannelBinder(){
        return new ActiveMqMessageChannelBinder(activemqUri);
    }
}
