package com.jaycode.framework.cloud.boot.core.lock;

import java.util.concurrent.TimeUnit;

/**
 * 抽象分布式锁
 *
 * @author jinlong.wang
 */
public interface Lock {

    /**
     * 阻塞式获取锁
     */
    void lock();

    void lock(long leaseTime, TimeUnit timeUnit);

    /**
     * 非阻塞式获取锁，如果获取失败立即返回false，否则返回true
     *
     * @return 获取锁结果
     */
    boolean tryLock();

    boolean tryLock(long leaseTime, TimeUnit timeUnit);

    /**
     * 释放锁
     */
    void unLock();
}
