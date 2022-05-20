package com.jaycode.framework.cloud.boot.starter.mongo;

import com.jaycode.framework.cloud.boot.core.data.PageRequest;
import com.jaycode.framework.cloud.boot.core.data.QueryRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import javax.annotation.Resource;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

@SuppressWarnings("unchecked")
public class MongoRepository<T> {

    private GeneralMongoRepository generalMongoRepository;


    @Resource
    public void setGeneralMongoRepository(GeneralMongoRepository generalMongoRepository) {
        this.generalMongoRepository = generalMongoRepository;
    }

    private Class<T> derive;

    public MongoRepository() {
        findDeriveClass();
    }


    /**
     * todo 不能理解
     */
    private void findDeriveClass() {
        try {
            Type sType = getClass().getGenericSuperclass();
            Type[] generics = ((ParameterizedType) sType).getActualTypeArguments();
            derive = (Class<T>) generics[0];
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public T findOne(Query query) {
        return (T) generalMongoRepository.findOne(derive, query);
    }


    public void insert(T t) {
        generalMongoRepository.insert(t);
    }

    public void update(T t) {
        generalMongoRepository.update(t);
    }

    public void update(Query query, Update update) {
        generalMongoRepository.update(derive, query, update);
    }

    public void remove(String id) {
        generalMongoRepository.remove(derive, id);
    }

    public List<T> find(Query query, QueryRequest request) {
        return generalMongoRepository.find(derive, query, request);

    }

    public List<T> find(Query query, PageRequest request) {
        return generalMongoRepository.find(derive, query, request);
    }

    public Long count(Query query) {
        return generalMongoRepository.count(derive, query);
    }

    public List<T> find(Query query) {
        return generalMongoRepository.find(derive, query);
    }

    public T findById(String id) {
        return generalMongoRepository.findById(derive, id);
    }

    protected List<T> findAll() {
        return generalMongoRepository.findAll(derive);
    }

    public MongoTemplate getMongoTemplate() {
        return generalMongoRepository.getMongoTemplate();
    }

}
