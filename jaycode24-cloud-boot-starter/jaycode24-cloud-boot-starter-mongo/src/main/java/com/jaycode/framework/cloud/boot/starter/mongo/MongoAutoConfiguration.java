package com.jaycode.framework.cloud.boot.starter.mongo;

import cn.hutool.core.util.StrUtil;
import com.jaycode.framework.cloud.boot.core.support.connection.ConnectionUtils;
import com.jaycode.framework.cloud.boot.starter.mongo.sequence.MongoSequenceRepository;
import com.mongodb.MongoClientURI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDbFactory;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * mongo主配置类
 * @author cheng.wang
 * @date 2022/5/19
 */
@Configuration
//开启mongoRepository自动扫描功能
@EnableMongoRepositories(basePackages = "com.jaycode")
@Slf4j
public class MongoAutoConfiguration {

    @Value("${jaycode.mongo.uri}")
    private String mongoUri;
    @Value("${jaycode.mongo.username:}")
    private String mongoUsername;
    @Value("${jaycode.mongo.password}")
    private String mongoPassword;

    public MongoAutoConfiguration() {
      log.info("mongo自动配置初始化开始。。。");
    }

    @Bean
    public MongoTemplate mongoTemplate(){
        MongoClientURI mongoClientURI = parseMongoClientURI();
        return new MongoTemplate(new SimpleMongoDbFactory(mongoClientURI));
    }

    private MongoClientURI parseMongoClientURI() {
        String mongoUri = this.mongoUri;
        if (!StrUtil.isEmpty(mongoPassword)) {
            mongoUri = mongoUri.replace(ConnectionUtils.PASSWORD_VAR, ConnectionUtils.decode(mongoPassword));
        }
        if (!StrUtil.isEmpty(mongoUsername)) {
            mongoUri = mongoUri.replace(ConnectionUtils.USERNAME_VAR, ConnectionUtils.decode(mongoUsername));
        }
        return new MongoClientURI(mongoUri);
    }

    @Bean
    public GeneralMongoRepository generalMongoRepository(MongoTemplate mongoTemplate) {
        return new GeneralMongoRepository(mongoTemplate);
    }

    @Bean
    public MongoSequenceRepository mongoSequenceRepository(GeneralMongoRepository generalMongoRepository) {
        return new MongoSequenceRepository(generalMongoRepository);
    }
}
