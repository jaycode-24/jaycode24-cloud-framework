package com.jaycode.cloud.boot.starter.stream.support;

import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

/**
 * @author cheng.wang
 * @date 2022/5/20
 */
public interface ChannelTemplate {

    SubscribableChannel input();

    MessageChannel output();
}
