package com.jaycode.framework.cloud.boot.starter.redis;

import io.lettuce.core.RedisURI;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisNode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author cheng.wang
 * @date 2022/5/19
 */
@Slf4j
@Data
public class RedisNodeInfo {

    private boolean cluster = false;

    private Integer database;

    private List<RedisNode> nodes = new ArrayList<>();
    private String password;


    public RedisNodeInfo(String redis){
        if (redis.startsWith("redis-cluster")){
            //集群
            log.info("启用redis集群服务");
            redis = redis.replace("redis-cluster://", "")
                    .replace("/", "");
            addNodes(redis);
            setCluster(true);
        }else {
            RedisURI redisURI = RedisURI.create(redis);
            if (redisURI.getPassword() != null && redisURI.getPassword().length > 0){
                setPassword(new String(redisURI.getPassword()));
            }
            nodes.add(new RedisNode(redisURI.getHost(),redisURI.getPort()));
        }
    }

    public void addNodes(String redis) {
        String[] servers = redis.split(",");
        for (String s : servers) {
            String[] splitArray = s.split(":");
            nodes.add(new RedisNode(splitArray[0], Integer.valueOf(splitArray[1])));
        }
    }
}
