package com.example.cache.service.impl;

import com.example.cache.CacheUtil;
import com.example.cache.dao.CacheDemo;
import com.example.cache.service.GetCacheService;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class GetCacheServiceImpl implements GetCacheService {
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private CacheManager cacheManager;

    /**
     * 注解方式，使用默认生成key
     * @param var1
     * @return
     */
    @CacheUtil.Cacheable("strongCache-get1")
    @Override
    public Object get1(int var1) {
        System.out.println("get1：未走缓存");
        return var1;
    }

    /**
     * 注解方式，使用SpEL表达式方式自定义key
     * @param var1
     * @param var2
     * @return
     */
    @CacheUtil.Cacheable(value = "strongCache-get2", key = "#var1 +'_'+ #var2")
    @Override
    public Object get2(int var1, int var2) {
        System.out.println("get2：未走缓存");
        return var1 + "_" + var2;
    }

    /**
     * 注解方式，使用默认生成key,形参是对象
     * @param cacheDemo
     * @return
     */
    @CacheUtil.Cacheable("strongCache-get3")
    @Override
    public Object get3(CacheDemo cacheDemo) {
        System.out.println("get3：未走缓存");
        return cacheDemo.toString();
    }

    /**
     *使用工具方法更新数据库，配合cacheable缓存数据,key要指定
     * @param var1
     * @param var2
     * @return
     */
    @Cacheable(value = "strongCache-get3",key = "#var1 +'_'+ #var2")
    @Override
    public Object get4(int var1, int var2) {
        CacheUtil build = new CacheUtil.AvoidBreakdownBuilder(redissonClient, cacheManager, "strongCache-get3", var1 + "_" + var2).build();
        try {
            return build.avoidBreakdown(() -> (CacheUtil.LockSuccess) () -> {
                System.out.println("get4：未走缓存");
                return var1 + "_" + var2;
            });
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return null;
        }
    }
}
