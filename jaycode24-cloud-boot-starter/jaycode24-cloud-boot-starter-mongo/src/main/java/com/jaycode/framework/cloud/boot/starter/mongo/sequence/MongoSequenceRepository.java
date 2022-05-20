package com.jaycode.framework.cloud.boot.starter.mongo.sequence;

import com.jaycode.framework.cloud.boot.starter.mongo.GeneralMongoRepository;
import com.jaycode.framework.cloud.boot.starter.mongo.MongoRepository;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

/**
 * @author cheng.wang
 * @date 2022/5/19
 */
public class MongoSequenceRepository extends MongoRepository<Sequence> implements DocumentSequenceRepository{

    public MongoSequenceRepository(GeneralMongoRepository generalMongoRepository){
        setGeneralMongoRepository(generalMongoRepository);
    }

    @Override
    public int generateNextSequence(String type) {
        Sequence sequence = this.getMongoTemplate().findAndModify(
                Query.query(Criteria.where("_id").is(type)),
                (new Update()).inc("seq", 1),
                FindAndModifyOptions.options().upsert(true).returnNew(true),
                Sequence.class);
        return sequence.getSeq();
    }
}
