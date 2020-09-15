package com.example.cache.util.lock;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;


//@Component //使用此锁开启注释,不使用关闭
public class RedissonLock implements CacheUtilLockAbstract {
    @Autowired
    private RedissonClient redissonClient;

    @Override
    public Lock getLock(String key) {
        RLock lock = redissonClient.getLock(key);
        return new Lock() {

            @Override
            public boolean tryLock(long expire, TimeUnit timeUnit) throws InterruptedException {
                return lock.tryLock(expire,timeUnit);
            }

            @Override
            public void unlock() {
                lock.unlock();
            }
        };
    }
}
