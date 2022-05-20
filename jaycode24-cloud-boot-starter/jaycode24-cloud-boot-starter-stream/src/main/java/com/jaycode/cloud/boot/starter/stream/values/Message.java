package com.jaycode.cloud.boot.starter.stream.values;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.messaging.support.MessageBuilder;

import java.beans.Transient;
import java.io.Serializable;
import java.util.UUID;

/**
 * @author cheng.wang
 * @date 2022/5/20
 */
@Data
public class Message implements Serializable, Payload {

    private static final long serialVersionUID = -4471286196444384021L;

    private String messageId = UUID.randomUUID().toString();

    public Message(){

    }


    @Override
    public String getId() {
        return this.messageId;
    }

    @Transient
    @JsonIgnore
    public org.springframework.messaging.Message toStreamMessage(){
        return MessageBuilder.withPayload(this).build();
    }
}
