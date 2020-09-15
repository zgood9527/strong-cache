package com.example.cache.util.lock;


import java.util.concurrent.TimeUnit;

public interface CacheUtilLockAbstract {
    Lock getLock(String key);

    interface Lock {
        boolean tryLock(long expire, TimeUnit timeUnit) throws Exception;

        void unlock() throws Exception;
    }
}
