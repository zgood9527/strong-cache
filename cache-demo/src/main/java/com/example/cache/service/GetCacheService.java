package com.example.cache.service;

import com.example.cache.dao.CacheDemo;

public interface GetCacheService {
    Object get1(int var1);

    Object get2(int var1, int var2);

    Object get3(CacheDemo cacheDemo);

    Object get4(int var1, int var2);
}
