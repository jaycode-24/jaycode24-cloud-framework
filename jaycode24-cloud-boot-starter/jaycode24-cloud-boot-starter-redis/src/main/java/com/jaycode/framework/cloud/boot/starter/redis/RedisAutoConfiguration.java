package com.jaycode.framework.cloud.boot.starter.redis;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.jaycode.framework.cloud.boot.core.support.connection.ConnectionUtils;
import com.jaycode.framework.cloud.boot.starter.redis.serializer.NullValueSerializer;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConfiguration;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.Serializable;

/**
 *
 * redis 初始化配置，
 *  * Spring 缓存管理器初始化配置
 *  * 命名空间设计：
 *  * 集群模式下不支持多db ，所以采用分组实现命名空间
 * @author cheng.wang
 * @date 2022/5/19
 */
@Configuration
@Slf4j
public class RedisAutoConfiguration extends CachingConfigurerSupport {

    private RedisNodeInfo redisNode;

    @Value("${jaycode.redis.uri}")
    private String redisUri;

    @Value("${jaycode.redis.namespace:}")
    private String namespace;

    @Value("${jaycode.redis.password:}")
    private String redisPassword;

    //redission连接数
    @Value("${jaycode.redis.redissionConnectionSize:-1}")
    private int redissionConnectionSize;

    @PostConstruct
    public void start() throws Exception {
        log.info("Redis 自动配置初始化:{}");
        redisNode = new RedisNodeInfo(redisUri);
        if (StringUtils.hasLength(redisPassword)) {
            redisNode.setPassword(ConnectionUtils.decode(redisPassword));
        }
        log.info("Redis 配置加载完成");
    }

    @Bean
    public KeyGenerator keyGenerator() {
        return (target, method, params) -> {
            StringBuilder sb = new StringBuilder();
            sb.append(target.getClass().getName());
            sb.append(method.getName());
            for (Object obj : params) {
                sb.append(obj.toString());
            }
            //log.debug(() -> "Generate new redis key " + sb.toString());
            return sb.toString();
        };
    }

    // 缓存管理器
    @Bean
    public RedisCacheFactoryBean redisCacheFactoryBean(LettuceConnectionFactory connectionFactory) {
        return new RedisCacheFactoryBean(connectionFactory);
    }

    /**
     * 构造redisTemplate，底层采用fastjson 作为序列化实现
     * 通过近几年等保测试发现，fastjson设计安全性和稳定性严重不足，索引采用fastjson 实现，避免底层框架出现安全或则稳定性漏洞
     *
     * @return rt 模板操作
     */
    @Bean
    public RedisTemplate<String, Serializable> redisCacheTemplate(LettuceConnectionFactory connectionFactory) {
        RedisTemplate<String, Serializable> template = new RedisTemplate<String, Serializable>();
        //设置unknow_properties处理策略，这样如果序列化数据存在当前运用class不存在的属性时候就会忽略，而不是报错
        //参照GenericJackson2JsonRedisSerializer(mapper)构造objectMapper对象，完成json 反序列化
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        //子类型找不到的时候忽略返回null，而非报错
        objectMapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
        //添加自定义序列化策略json 序列化方式为增加@class属性，其值为class name
        objectMapper.registerModule(new SimpleModule().addSerializer(new NullValueSerializer(null)));
        objectMapper.activateDefaultTyping(objectMapper.getPolymorphicTypeValidator(), ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        //使用jackson2 作为redis 对象序列化工具
        GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        template.setKeySerializer(new RedisKeySerialize(namespace));
        //序列化注意属性问题，如果属性改名则会导致缓存数据失效
        template.setValueSerializer(genericJackson2JsonRedisSerializer);
        template.setHashKeySerializer(new RedisKeySerialize(namespace));// Hash key序列化
        template.setHashValueSerializer(genericJackson2JsonRedisSerializer);// Hash value序列化
        template.setConnectionFactory(connectionFactory);
        return template;

    }

    /**
     * 对hash类型的数据操作
     *
     * @param redisTemplate
     * @return
     */
    @Bean
    public HashOperations<String, String, Serializable> hashOperations(RedisTemplate<String, Serializable> redisTemplate) {
        return redisTemplate.opsForHash();
    }

    /**
     * 对redis字符串类型数据操作
     *
     * @param redisTemplate
     * @return
     */
    @Bean
    public ValueOperations<String, Serializable> valueOperations(RedisTemplate<String, Serializable> redisTemplate) {
        return redisTemplate.opsForValue();
    }

    /**
     * 对链表类型的数据操作
     *
     * @param redisTemplate
     * @return
     */
    @Bean
    public ListOperations<String, Serializable> listOperations(RedisTemplate<String, Serializable> redisTemplate) {
        return redisTemplate.opsForList();
    }

    /**
     * 对无序集合类型的数据操作
     *
     * @param redisTemplate
     * @return
     */
    @Bean
    public SetOperations<String, Serializable> setOperations(RedisTemplate<String, Serializable> redisTemplate) {
        return redisTemplate.opsForSet();
    }

    /**
     * 对有序集合类型的数据操作
     *
     * @param redisTemplate
     * @return
     */
    @Bean
    public ZSetOperations<String, Serializable> zSetOperations(RedisTemplate<String, Serializable> redisTemplate) {
        return redisTemplate.opsForZSet();
    }

    @Bean
    public LettuceConnectionFactory lettuceConnectionFactory(){
        RedisConfiguration redisConfiguration;
        if (redisNode.isCluster()){
            //集群配置
            redisConfiguration = new RedisClusterConfiguration();
            for (RedisNode node : redisNode.getNodes()) {
                RedisClusterConfiguration redisClusterConfiguration = (RedisClusterConfiguration) redisConfiguration;
                redisClusterConfiguration.addClusterNode(node);
                redisClusterConfiguration.setPassword(redisNode.getPassword());
            }

        }else {
            RedisNode rn = redisNode.getNodes().get(0);
            redisConfiguration = new RedisStandaloneConfiguration();
            RedisStandaloneConfiguration redisStandaloneConfiguration = (RedisStandaloneConfiguration) redisConfiguration;
            redisStandaloneConfiguration.setHostName(rn.getHost());
            redisStandaloneConfiguration.setPort(rn.getPort());
            redisStandaloneConfiguration.setPassword(redisNode.getPassword());
        }
        return new LettuceConnectionFactory(redisConfiguration);
    }

    @Bean(destroyMethod = "shutdown", name = "redissonClient")
    //@ConditionalOnClass(name = {"javax.servlet.http.HttpServletResponse"})
    public RedissonClient redisson() throws IOException {
        return Redisson.create(createRedissionConfig());
    }


    private Config createRedissionConfig() {
        Config redisConfiguration = new Config();
        if (redisNode.isCluster()) {
            //这是用的集群server
            //设置集群状态扫描时间
            //集群模式redission没有设是指db 的路径
            for (RedisNode rn : redisNode.getNodes()) {
                redisConfiguration.useClusterServers()
                        //兼容redession bug，必须添加redis前缀才能正常添加节点信息
                        .addNodeAddress("redis://" + rn.asString());
            }
            ClusterServersConfig clusterServersConfig = redisConfiguration.useClusterServers();
            if (redissionConnectionSize != -1) {
                clusterServersConfig.setSlaveConnectionMinimumIdleSize(redissionConnectionSize);
            }
            if (redissionConnectionSize != -1) {
                clusterServersConfig.setMasterConnectionMinimumIdleSize(redissionConnectionSize);
            }
            clusterServersConfig.setPassword(redisNode.getPassword());
        } else {
            SingleServerConfig singleServerConfig = redisConfiguration.useSingleServer()
                    .setPassword(redisNode.getPassword())
                    .setAddress("redis://" + redisNode.getNodes().get(0).toString());
            if (redissionConnectionSize != -1) {
                singleServerConfig.setConnectionMinimumIdleSize(redissionConnectionSize);
            }
        }
        return redisConfiguration;
    }

    @Bean
    public RedisLockManager redisLockManager(RedissonClient redissonClient) {
        return new RedisLockManager(redissonClient, namespace);
    }

    private static class RedisKeySerialize extends StringRedisSerializer {
        private String namespace = "";

        public RedisKeySerialize(String namespace) {
            if (!StringUtils.isEmpty(namespace)) {
                this.namespace = namespace + ":";
            }
        }

        @Override
        public byte[] serialize(String string) {
            return super.serialize(namespace + string);
        }
    }
}
