package com.jaycode.framework.cloud.boot.starter.mongo;

import com.jaycode.framework.cloud.boot.core.data.PageRequest;
import com.jaycode.framework.cloud.boot.core.data.PagedList;
import com.jaycode.framework.cloud.boot.core.data.QueryRequest;
import com.jaycode.framework.cloud.boot.core.entity.EntityManager;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;

/**
 * 通用mongo仓库
 * @author cheng.wang
 * @date 2022/5/19
 */
public class GeneralMongoRepository {

    private final EntityManager entityManager = EntityManager.getInstance();
    private final MongoTemplate mongoTemplate;


    public GeneralMongoRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public <T> T findOne(Class<T> derive, Query query){
        return mongoTemplate.findOne(query,derive);
    }

    public <T> void insert(T t){
        if (entityManager.isEntityObject(t)){
            entityManager.preparePersist(t);
        }
        mongoTemplate.insert(t);
    }

    public <T> void update(T t) {
        mongoTemplate.save(t);
    }

    public <T> void remove(Class<T> derive, String id) {
        mongoTemplate.remove(findById(derive, id));
    }

    public <T> T findById(Class<T> derive, String id) {
        return mongoTemplate.findById(id, derive);
    }

    public <T> List<T> find(Class<T> derive, Query query, QueryRequest request) {
        PageRequest pageable = request.getPageRequest();
        if (pageable != null && pageable.isAvailable()) {
            int startRow = pageable.getStartRow();
            query.skip(startRow).limit(pageable.getPageSize());
            if (!query.getSortObject().isEmpty()) {
                //存在排序且未包含主键，则自动加上，用于修复排序值相同的情况下，排序错乱的问题
                /*if (!query.getSortObject().containsKey("_id")) {
                    query.with(Sort.by(Sort.Direction.ASC, "_id"));
                }*/
            }
            return new PagedList<T>(mongoTemplate.find(query, derive), Long.valueOf(mongoTemplate.count(query, derive)).intValue());
        } else {
            return mongoTemplate.find(query, derive);
        }

    }

    public <T> List<T> find(Class<T> derive, Query query, PageRequest request) {
        if (request != null && request.isAvailable()) {
            int startRow = request.getStartRow();
            query.skip(startRow).limit(request.getPageSize());
            if (!query.getSortObject().isEmpty()) {
                //存在排序且未包含主键，则自动加上
               /* if (!query.getSortObject().containsKey("_id")) {
                    query.with(Sort.by(Sort.Direction.DESC, "_id"));
                }*/
            }
            return new PagedList<T>(mongoTemplate.find(query, derive), Long.valueOf(mongoTemplate.count(query, derive)).intValue());
        } else {
            return mongoTemplate.find(query, derive);
        }

    }

    public <T> void update(Class<T> derive, Query query, Update update) {
        mongoTemplate.updateFirst(query, update, derive);
    }

    public <T> List<T> find(Class<T> derive, Query query) {
        return mongoTemplate.find(query, derive);
    }

    public <T> List<T> findAll(Class<T> derive) {
        return mongoTemplate.findAll(derive);
    }
    public <T> Long count(Class<T> derive, Query query) {
        return mongoTemplate.count(query, derive);
    }

    public MongoTemplate getMongoTemplate() {
        return mongoTemplate;
    }
}
