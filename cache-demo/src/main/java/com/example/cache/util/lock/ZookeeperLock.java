package com.example.cache.util.lock;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreMutex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component  //使用此锁开启注释,不使用关闭
public class ZookeeperLock implements CacheUtilLockAbstract {

    @Autowired
    private CuratorFramework curatorFramework;

    @Override
    public Lock getLock(String key) {
        InterProcessSemaphoreMutex lock = new InterProcessSemaphoreMutex(curatorFramework, "/"+key);
        return new Lock() {
            @Override
            public boolean tryLock(long expire, TimeUnit timeUnit) throws Exception {
                return lock.acquire(10,timeUnit);//防止阻塞拿锁
            }

            @Override
            public void unlock() throws Exception {
                lock.release();
            }
        };
    }
}
