package com.jaycode.framework.cloud.boot.core.lock;

/**
 * 分布式锁管理器
 *
 * @author jinlong.wang
 */
public interface LockManager {
    /**
     * 根据key创建锁对象
     *
     * @param key 锁ID
     * @return 锁对象
     */
    Lock createLock(String key);
}
