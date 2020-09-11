package com.example.cache.service.impl;

import com.example.cache.dao.CacheDemo;
import com.example.cache.service.DeleteCacheService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

@Service
public class DeleteCacheServiceImpl implements DeleteCacheService {

    @CacheEvict("strongCache-get1")
    @Override
    public void delete1(int var1) {
        System.out.println("删除缓存：delete1");
    }

    @CacheEvict(value = "strongCache-get2", key = "#var1+'_'+#var2")
    @Override
    public void delete2(int var1, int var2) {
        System.out.println("删除缓存：delete2");
    }

    @CacheEvict("strongCache-get3")
    @Override
    public void delete3(CacheDemo cacheDemo) {
        System.out.println("删除缓存：delete3");
    }
}
