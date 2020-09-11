package com.example.cache.controller;

import com.example.cache.dao.CacheDemo;
import com.example.cache.service.DeleteCacheService;
import com.example.cache.service.GetCacheService;
import com.example.cache.service.UpdateCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("strongCache")
public class DemoController {

    @Autowired
    private GetCacheService getCacheService;
    @Autowired
    private DeleteCacheService deleteCacheService;
    @Autowired
    private UpdateCacheService updateCacheService;

    //TODO -----------------获取-----------------------
    @GetMapping("/get1/{var1}")
    public Object get1(@PathVariable int var1) {
        return getCacheService.get1(var1);
    }

    @GetMapping("/get2")
    public Object get2(int var1, int var2) {
        return getCacheService.get2(var1, var2);
    }

    @PostMapping(value = "/get3")
    public Object get3(@RequestBody CacheDemo cacheDemo) {
        return getCacheService.get3(cacheDemo);
    }

    //TODO -----------------删除-----------------------
    @DeleteMapping("/delete1/{var1}")
    public void delete1(@PathVariable int var1) {
        deleteCacheService.delete1(var1);
    }

    @DeleteMapping("/delete2")
    public void delete2(int var1, int var2) {
        deleteCacheService.delete2(var1, var2);
    }

    @DeleteMapping("/delete3")
    public void delete3(@RequestBody CacheDemo cacheDemo) {
        deleteCacheService.delete3(cacheDemo);
    }

    //TODO -----------------更新-----------------------
    @PutMapping("/update1/{id}")
    public void update1(@PathVariable int id) {
        updateCacheService.update1(id);
    }

    @PutMapping("/update2")
    public void update2(int var1, int var2) {
        updateCacheService.update2(var1, var2);
    }

    @PutMapping("/update3")
    public void update3(@RequestBody CacheDemo cacheDemo) {
        updateCacheService.update3(cacheDemo);
    }
}
