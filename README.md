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
### 3方法上添加注解
在需要获取数据并缓存的方法上添加@CacheUtil-Cacheable()注解
## 注解中各参数
value           缓存区域,类似在redis创建了一个文件夹，默认取类名加方法名
cacheNames      缓存区域同value
key             缓存区域下的键，一般由方法形参的值组成，默认生成方式参照SimpleKeyGenerator
lockKey         分布式锁的名，默认跟value,key相关
expire          锁失效时间，毫秒值,默认5000
sleepMillis     拿锁失败后睡眠时间，毫秒值，默认90-120随机数

## 实现原理
![Image text](http://res.andybaby-edu.com/%E9%98%B2%E7%BC%93%E5%AD%98%E5%87%BB%E7%A9%BF.jpg)
