package com.example.cache;

import com.example.cache.dao.CacheDemo;
import com.example.cache.service.DeleteCacheService;
import com.example.cache.service.GetCacheService;
import com.example.cache.service.UpdateCacheService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CacheDemoApplicationTests {

    @Autowired
    private GetCacheService getCacheService;
    @Autowired
    private DeleteCacheService deleteCacheService;
    @Autowired
    private UpdateCacheService updateCacheService;

    @Test
    void getCache() {
        Object getCacheService1 = getCacheService.get1(1);
        System.out.println("get1:"+getCacheService1.toString());
        Object getCacheService2 = getCacheService.get2(1, 2);
        System.out.println("get2:"+getCacheService2.toString());
        Object getCacheService3 = getCacheService.get3(new CacheDemo());
        System.out.println("get3:"+getCacheService3.toString());
    }

    @Test
    void deleteCache() {
        deleteCacheService.delete1(1);
        deleteCacheService.delete2(1,2);
        deleteCacheService.delete3(new CacheDemo());
    }

    @Test
    void updateCache() {
        updateCacheService.update1(1);
        updateCacheService.update2(1,2);
        updateCacheService.update3(new CacheDemo());
    }
}
