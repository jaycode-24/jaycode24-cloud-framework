package com.jaycode.cloud.boot.starter.stream;

import cn.hutool.core.util.StrUtil;
import com.jaycode.cloud.boot.starter.stream.annotation.StreamChannel;
import com.jaycode.cloud.boot.starter.stream.support.AopDistributedLockAspect;
import com.jaycode.framework.cloud.boot.core.config.Config;
import com.jaycode.framework.cloud.boot.core.config.ConfigFactory;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * @author cheng.wang
 * @date 2022/5/19
 */
@Slf4j
@Configuration
@ImportAutoConfiguration({
        ActiveMqStreamAutoConfiguration.class,
        ActiveMqStreamOldAutoConfiguration.class,
        KafkaAutoConfiguration.class
})
public class StreamAutoConfiguration {

    private static final String NS_CONFIG_KEY = "jaycode.stream.namespace";
    private static final String PACKAGE_PREFIX = "com.jaycode.cloud";
    public static final String SPRING_STREAM_CONFIG_PREFIX = "spring.cloud.stream";

    static {
        autoSetStreamBindsSystemPropertyMeta();
    }

    /**
     *      * 初始化，放入System.property中
     */
    private static void autoSetStreamBindsSystemPropertyMeta() {
        Config config = ConfigFactory.getShareConfig();
        Reflections reflections = new Reflections(PACKAGE_PREFIX);
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(StreamChannel.class);
        String namespace = config.getValue(NS_CONFIG_KEY);
        for (Class<?> cls : classes) {
            try {
                StreamChannel streamChannel = cls.getAnnotation(StreamChannel.class);
                String topicName = streamChannel.value();
                if (StrUtil.isNotBlank(namespace)){
                    topicName = namespace + "." + topicName;
                }
                Method[] methods = cls.getMethods();
                for (Method method : methods) {
                    if ("input".equals(method.getName()) || method.getReturnType().equals(SubscribableChannel.class)){
                        Input binder = method.getAnnotation(Input.class);
                        if (binder != null){
                            String binderName = binder.value();
                            initProperties(binderName,topicName);
                        }
                    }
                    if ("output".equals(method.getName()) || method.getReturnType().equals(MessageChannel.class)){
                        Output binder = method.getAnnotation(Output.class);
                        if (binder != null){
                            String binderName = binder.value();
                            initProperties(binderName,topicName);

                        }
                    }
                }
                log.debug("完成" + cls.getSimpleName() + "Stream通道设置，通道名为:" + topicName);

            }catch (Exception e){
                throw new IllegalArgumentException("不规范的Stream 通道定义", e);

            }
        }
    }

    private static void initProperties(String inputTarget, String value) {
        if (inputTarget.contains(".")) {
            throw new StreamException("不规范的Stream Bean 名称定义，不能包含\".\"符号");
        }
        System.setProperty(SPRING_STREAM_CONFIG_PREFIX + ".bindings." + inputTarget + ".destination", value);
        System.setProperty(SPRING_STREAM_CONFIG_PREFIX + ".bindings." + inputTarget + ".contentType", "application/json");
    }

    @Bean
    public AopDistributedLockAspect aopDistributedLockAspect() {
        return new AopDistributedLockAspect();
    }
}
