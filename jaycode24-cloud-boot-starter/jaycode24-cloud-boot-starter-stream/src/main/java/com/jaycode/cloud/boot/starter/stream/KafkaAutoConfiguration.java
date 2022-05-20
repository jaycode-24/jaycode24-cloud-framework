package com.jaycode.cloud.boot.starter.stream;

import cn.hutool.core.util.StrUtil;
import com.jaycode.framework.cloud.boot.core.config.Config;
import com.jaycode.framework.cloud.boot.core.config.ConfigFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * kafka 自动配置，并在类加载时候注入kafka相关system properties，以便相关组件完成初始化操作
 * @author cheng.wang
 * @date 2022/5/19
 */
@Slf4j
@Configuration
public class KafkaAutoConfiguration {


    public static final String CONFIG_PREFIX = "jaycode.stream.kafka";
    private static final String CONFIG_BROKERS = CONFIG_PREFIX + ".brokers";
    private static final String SPRING_STREAM_KAFKA_PREFIX = "spring.cloud.stream.kafka.binder";

    static {
        Config config = ConfigFactory.getShareConfig();
        if (config.has(CONFIG_PREFIX)){
            log.info("发现KAFKA的MQ中间件配置，传递配置到Java SystemProperties");
            String brokers = config.getValue(CONFIG_BROKERS);
            System.setProperty(SPRING_STREAM_KAFKA_PREFIX + ".brokers",brokers);
            String zkAddress = config.getValue(CONFIG_PREFIX + ".zk-nodes");
            if (StrUtil.isNotBlank(zkAddress)){
                System.setProperty(SPRING_STREAM_KAFKA_PREFIX + ".zk-nodes",zkAddress);
            }
            System.setProperty(SPRING_STREAM_KAFKA_PREFIX + ".auto-create-topics", String.valueOf(config.getValue(CONFIG_PREFIX + ".auto-create-topics", true)));


        }
    }
}
