package com.flashmall.common.lock;

import java.util.concurrent.TimeUnit;

/**
 * 分布式锁服务
 */
public interface DistributedLockService {

    /**
     * 尝试获取锁（非阻塞式，避免请求无限等待）
     *
     * @param lockKey   锁 Key
     * @param waitTime  等待获取锁的最长时间
     * @param leaseTime 锁自动释放时间（超过后自动解锁，防止死锁）
     * @param unit      时间单位
     * @return true=获取成功，false=获取失败
     */
    boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit);

    /**
     * 释放锁（只有当前线程持有锁时才释放）
     */
    void unlock(String lockKey);
}
