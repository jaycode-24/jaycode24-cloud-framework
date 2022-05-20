package com.jaycode.framework.cloud.boot.starter.mongo.sequence;

public interface DocumentSequenceRepository {
    int generateNextSequence(String type);
}
