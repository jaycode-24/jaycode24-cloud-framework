package com.jaycode.framework.cloud.boot.starter.redis;

import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 分布式缓存工厂
 * @author cheng.wang
 * @date 2022/5/19
 */
public class RedisCacheFactoryBean {

    private RedisConnectionFactory redisConnectionFactory;

    public RedisCacheFactoryBean(RedisConnectionFactory redisConnectionFactory) {
        this.redisConnectionFactory = redisConnectionFactory;
    }

    public CacheBuilder builder() {
        return new CacheBuilder(redisConnectionFactory);
    }

    public static class CacheBuilder{
        Map<String, RedisCacheConfiguration> configurationMap = new HashMap<>();
        private RedisConnectionFactory redisConnectionFactory;

        public CacheBuilder(RedisConnectionFactory redisConnectionFactory) {
            this.redisConnectionFactory = redisConnectionFactory;
        }

        public CacheBuilder create(String cacheName, Duration duration) {
            configurationMap.put(cacheName, RedisCacheConfiguration.defaultCacheConfig().entryTtl(duration));
            return this;
        }


        public CacheManager build() {
            RedisCacheManager.RedisCacheManagerBuilder builder = RedisCacheManager.RedisCacheManagerBuilder
                    .fromConnectionFactory(redisConnectionFactory);
            builder.withInitialCacheConfigurations(configurationMap);
            return builder.build();
        }


    }

}
