package com.flashmall.common.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class DistributedLockServiceImpl implements DistributedLockService {

    private final RedissonClient redissonClient;

    @Override
    public boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            return lock.tryLock(waitTime, leaseTime, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("[DistributedLock] 获取锁被中断 lockKey={}", lockKey, e);
            return false;
        }
    }

    @Override
    public void unlock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        // 只有当前线程持有锁时才释放，避免误释放其他线程/请求的锁
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
            log.debug("[DistributedLock] 锁已释放 lockKey={}", lockKey);
        }
    }
}
