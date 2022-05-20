package com.jaycode.framework.cloud.boot.starter.mongo.sequence;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Entity;

@Document(
        collection = "sequence"
)
@Data
@Entity
public class Sequence {
    @Id
    @javax.persistence.Id
    private String biz;
    private int seq;

    public Sequence() {
    }
}