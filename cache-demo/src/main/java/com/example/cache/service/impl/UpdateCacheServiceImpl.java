package com.example.cache.service.impl;

import com.example.cache.dao.CacheDemo;
import com.example.cache.service.DeleteCacheService;
import com.example.cache.service.GetCacheService;
import com.example.cache.service.UpdateCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 为了保证缓存一致性，将更新缓存的操作交给@CacheUtil.Cacheable注解
 * 当有缓存需要更新的时候，调用删除缓存后再调用获取功能就可以保证一致性更新缓存
 */
@Service
public class UpdateCacheServiceImpl implements UpdateCacheService {

    @Autowired
    private GetCacheService getCacheService;
    @Autowired
    private DeleteCacheService deleteCacheService;

    @Override
    public void update1(int var1) {
        System.out.println("更新缓存：update1");
        deleteCacheService.delete1(var1);
        getCacheService.get1(var1);
    }

    @Override
    public void update2(int var1, int var2) {
        System.out.println("更新缓存：update2");
        deleteCacheService.delete2(var1,var2);
        getCacheService.get2(var1,var2);
    }

    @Override
    public void update3(CacheDemo cacheDemo) {
        System.out.println("更新缓存：update3");
        deleteCacheService.delete3(cacheDemo);
        getCacheService.get3(cacheDemo);
    }
}
