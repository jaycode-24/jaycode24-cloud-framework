package com.jaycode.cloud.boot.starter.stream.values;

import lombok.Data;

import java.io.Serializable;

/**
 * @author cheng.wang
 * @date 2022/5/20
 */
@Data
public class SimpleMessage extends Message{
    private static final long serialVersionUID = 5234441110773454596L;

    private Serializable body;


}
