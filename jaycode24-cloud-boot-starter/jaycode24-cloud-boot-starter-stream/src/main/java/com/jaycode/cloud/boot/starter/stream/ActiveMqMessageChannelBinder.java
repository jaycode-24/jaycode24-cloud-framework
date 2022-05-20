package com.jaycode.cloud.boot.starter.stream;

import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQBytesMessage;
import org.springframework.cloud.stream.binder.*;
import org.springframework.context.Lifecycle;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.util.Assert;

import javax.jms.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 *
 * Spring jms与Spring stream 桥接，桥接后通过spring stream方式可直接发送和接受消息
 *  * 因为activemq 底层原因未实现Stream分组消息
 * @author cheng.wang
 * @date 2022/5/19
 */
@Slf4j
public class ActiveMqMessageChannelBinder extends AbstractBinder<MessageChannel, ConsumerProperties, ProducerProperties> {

    private final JmsTemplate jmsTemplate;

    public ActiveMqMessageChannelBinder(String activemqUri){
        JmsTemplate jmsTemplate = new JmsTemplate();
        //自动签收
        jmsTemplate.setSessionAcknowledgeMode(Session.AUTO_ACKNOWLEDGE);
        jmsTemplate.setConnectionFactory(new ActiveMQConnectionFactory(activemqUri));
        //false queue,true topic
        jmsTemplate.setPubSubDomain(true);
        this.jmsTemplate = jmsTemplate;
    }

    @Override
    protected Binding<MessageChannel> doBindConsumer(String name, String group, MessageChannel inputTarget, ConsumerProperties properties) {
        ActiveMqConnectionLifeCycle activeMqConnectionLifeCycle = new ActiveMqConnectionLifeCycle(jmsTemplate, name, inputTarget);
        activeMqConnectionLifeCycle.start();
        return new DefaultBinding<>(name,group,inputTarget,activeMqConnectionLifeCycle);
    }

    @Override
    protected Binding<MessageChannel> doBindProducer(String name, MessageChannel outboundBindTarget, ProducerProperties properties) {
        // MessageChannel 必须是 SubscribableChannel 类型
        Assert.isInstanceOf(SubscribableChannel.class, outboundBindTarget,
                "Binding is supported only for SubscribableChannel instances");

        //强转为 SubscribableChannel类型
        SubscribableChannel subscribableChannel = (SubscribableChannel) outboundBindTarget;

        subscribableChannel.subscribe(message -> {
            /* *     接收内部管道消息，来自于 MessageChannel#send(message)，实际并没有发送消息，
             * 而是此消息将要发送到 ActiveMQ Broker。
             *     案例：
             *     我们在调用 UserServiceClientController#saveUserByActiveMQStreamBinder() 方法时，
             * 会通过 messageChannel.send(message) 向 ActiveMQ 发送消息，而这里先拦截到该消息，再由
             * 这里转发至 ActiveMQ*/
            try {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
                objectOutputStream.writeObject(message);
                jmsTemplate.convertAndSend(name, byteArrayOutputStream.toByteArray());
            }catch (Exception e){
                throw new StreamException(e.getMessage(), e);

            }
        });
        return () -> log.info("释放Jms Producer资源");
    }

    private static class ActiveMqConnectionLifeCycle implements Lifecycle {
        private JmsTemplate jmsTemplate;
        private String topicName;
        private MessageChannel inputTarget;


        private boolean isRunning = false;
        private Connection connection;
        private Session session;


        public ActiveMqConnectionLifeCycle(JmsTemplate jmsTemplate, String topicName, MessageChannel inputTarget) {
            this.jmsTemplate = jmsTemplate;
            this.topicName = topicName;
            this.inputTarget = inputTarget;
        }

        @Override
        public void start() {
            //实现消息消费
            ConnectionFactory connectionFactory = jmsTemplate.getConnectionFactory();
            try {
                isRunning = true;
                Assert.notNull(connectionFactory, "JMS连接工厂不应为空");
                // 创造 JMS 链接
                //创建连接
                connection = connectionFactory.createConnection();
                // 启动连接
                connection.start();
                //创建会话 session，（关闭事务，自动签收）
                session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                // 创建消息目的
                Destination destination = session.createTopic(topicName);
                // 创建消息消费者
                MessageConsumer messageConsumer = session.createConsumer(destination);

                messageConsumer.setMessageListener(message -> {
                    try {
                        ActiveMQBytesMessage activeMQBytesMessage = (ActiveMQBytesMessage) message;
                        byte[] bytes = activeMQBytesMessage.getContent().getData();
                        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
                        inputTarget.send((Message<?>) objectInputStream.readObject());

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                });

            } catch (Exception e) {
                throw new StreamException(e.getMessage(), e);
            }

        }

        @Override
        public void stop() {
            log.info("释放Jms Consumer Binder资源");
            try {
                session.close();
            } catch (Exception e) {
                //
            }
            try {
                connection.close();
            } catch (Exception e) {
                //
            }
            isRunning = false;
        }

        @Override
        public boolean isRunning() {
            return isRunning;
        }
    }
}
