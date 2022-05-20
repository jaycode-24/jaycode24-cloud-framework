package com.jaycode.framework.cloud.boot.starter.redis;

import com.jaycode.framework.cloud.boot.core.lock.Lock;
import com.jaycode.framework.cloud.boot.core.lock.LockManager;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

/**
 * 基于redis实现分布式锁
 * @author cheng.wang
 * @date 2022/5/19
 */
public class RedisLockManager implements LockManager {

    private final RedissonClient redissonClient;

    private String namespace;

    public RedisLockManager(RedissonClient redissonClient, String namespace) {
        this.redissonClient = redissonClient;
        this.namespace = namespace;
    }

    @Override
    public Lock createLock(String key) {
        return new LockImpl(redissonClient.getLock(namespace + key));
    }

    public static class LockImpl implements Lock{
        private final RLock rLock;

        public LockImpl(RLock rLock) {
            this.rLock = rLock;
        }

        @Override
        public void lock() {
            rLock.lock();
        }

        @Override
        public void lock(long leaseTime, TimeUnit timeUnit) {
            rLock.lock(leaseTime, timeUnit);

        }

        @Override
        public boolean tryLock() {
            return rLock.tryLock();        }

        @Override
        public boolean tryLock(long leaseTime, TimeUnit timeUnit) {
            try {
                return rLock.tryLock(1, leaseTime, timeUnit);
            } catch (InterruptedException e) {
                return false;
            }
        }

        @Override
        public void unLock() {
            rLock.unlock();
        }
    }
}
