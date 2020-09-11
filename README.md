# StrongCacheUtil
用于防止缓存击穿的自动更新缓存注解，打上注解的方法只需要在其中实现查库逻辑返回数据即可，配合Spring官方的cache注解的@CacheEvict()达到保证缓存一致性的同时更新缓存（非强一致性）
## 使用方法
### 1所需依赖
```
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <!-- redis -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
        <!-- aop -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-aop</artifactId>
        </dependency>
        <!-- redisson 分布式锁 https://mvnrepository.com/artifact/org.redisson/redisson -->
        <dependency>
            <groupId>org.redisson</groupId>
            <artifactId>redisson</artifactId>
            <version>3.13.4</version>
        </dependency>
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-pool2</artifactId>
            <version>2.6.1</version>
        </dependency>
```
### 2添加工具类
将CacheUtil粘到项目中
### 2添加注解
在需要获取数据并缓存的方法上添加@CacheUtil-Cacheable()注解
## 实现原理
![Image text](http://res.andybaby-edu.com/%E9%98%B2%E7%BC%93%E5%AD%98%E5%87%BB%E7%A9%BF.jpg)
