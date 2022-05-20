package com.jaycode.cloud.boot.starter.stream;

import org.springframework.cloud.stream.binder.AbstractBinder;
import org.springframework.cloud.stream.binder.Binding;
import org.springframework.cloud.stream.binder.ConsumerProperties;
import org.springframework.cloud.stream.binder.ProducerProperties;
import org.springframework.messaging.MessageChannel;

/**
 * 空StreamBinder实现，避免在无任何mq配置的情况下spring kafka 自动初始化本地客户端
 *
 * @author jinlong.wang
 */
public class EmptyBinder extends AbstractBinder<MessageChannel,
        ConsumerProperties, ProducerProperties> {

    @Override
    protected Binding<MessageChannel> doBindConsumer(String name, String group, MessageChannel inputTarget, ConsumerProperties properties) {
        return null;
    }

    @Override
    protected Binding<MessageChannel> doBindProducer(String name, MessageChannel outboundBindTarget, ProducerProperties properties) {
        return null;
    }
}
