package com.example.cache.util;

import com.example.cache.util.lock.CacheUtilLockAbstract;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AliasFor;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 缓存处理工具类
 * 防止缓存击穿：防止并发下缓存某一刻失效导致大量请求怼到数据库
 * 支持CacheUtil.Cacheable注解方式和avoidBreakdown()方法方式
 */
public class CacheUtil2 {
    private final CacheUtilLockAbstract cacheUtilLockAbstract;
    private final CacheManager cacheManager;
    private final String cacheValue;
    private final String cacheKey;
    private final String lockKey;
    private final long expire;
    private final long sleepMillis;
    private final boolean isUseSelfCacheable;

    private CacheUtil2(AvoidBreakdownBuilder builder) {
        cacheUtilLockAbstract = builder.cacheUtilLockAbstract;
        cacheManager = builder.cacheManager;
        cacheValue = builder.cacheValue;
        cacheKey = builder.cacheKey;
        lockKey = builder.lockKey;
        expire = builder.expire;
        sleepMillis = builder.sleepMillis;
        isUseSelfCacheable = builder.isUseSelfCacheable;
    }

    /**
     * 防止缓存击穿方式更新缓存，更新缓存的逻辑交给调用方
     * 1初始查看一次缓存是否存在，存在返回缓存
     * 2缓存不存在尝试拿锁
     * 3拿锁成功，再获取一次缓存是否存在，存在则返回缓存，不存在则暴露给调用者获取缓存并存储缓存
     * 4拿锁失败进入休眠，唤醒后从第1步开始重复，直至获取到缓存
     *
     * @param lockSuccess 抢锁成功后的操作回调
     * @return
     */
    public Object avoidBreakdown(LockSuccess lockSuccess) throws Throwable {
        while (true) {
            //初始或者唤醒后获取一次缓存，为了减少拿锁次数
            Cache cache = cacheManager.getCache(cacheValue);
            if (cache != null) {
                Cache.ValueWrapper valueWrapper = cache.get(cacheKey);
                if (valueWrapper != null) {
                    return valueWrapper.get();
                }
            }
            CacheUtilLockAbstract.Lock lock = cacheUtilLockAbstract.getLock(lockKey);
            try {
                boolean isLock = lock.tryLock(expire, TimeUnit.MILLISECONDS);
                if (isLock) {
                    try {
                        //加锁后获取一次缓存，有就返回数据，没有就查库返回数据
                        Cache cache2 = cacheManager.getCache(cacheValue);
                        if (cache2 != null) {
                            Cache.ValueWrapper valueWrapper2 = cache2.get(cacheKey);
                            if (valueWrapper2 != null) {
                                return valueWrapper2.get();
                            }
                        }
                        Object update = lockSuccess.update();
                        //注解自动更新缓存
                        if (isUseSelfCacheable) {
                            Cache cache3 = cacheManager.getCache(cacheValue);
                            if (cache3 == null) {
                                Collection<String> cacheNames = cacheManager.getCacheNames();
                                cacheNames.add(cacheValue);
                                Cache cache4 = cacheManager.getCache(cacheValue);
                                if (cache4 != null) {
                                    cache4.put(cacheKey, update);
                                }
                            } else {
                                if (cache3.get(cacheKey) == null)
                                    cache3.put(cacheKey, update);
                            }
                        }
                        return update;
                    } finally {
                        lock.unlock();
                    }
                } else {
                    Thread.sleep(sleepMillis);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    public interface LockSuccess {
        Object update() throws Throwable;
    }

    /**
     * 防止缓存击穿建造类
     */
    public static class AvoidBreakdownBuilder {
        private CacheUtilLockAbstract cacheUtilLockAbstract;
        private CacheManager cacheManager;//缓存管理类
        private String cacheValue;//缓存域名
        private String cacheKey;//缓存域名下的键
        private String lockKey;//锁的键，有默认
        private long expire = 5000;//缓存失效时间，毫秒值
        private long sleepMillis = 90 + (int) (Math.random() * 30);//拿锁失败睡眠时间，毫秒值，90-120随机数
        private boolean isUseSelfCacheable = false;//是否使用了标签

        public AvoidBreakdownBuilder(CacheUtilLockAbstract cacheUtilLockAbstract, CacheManager cacheManager, String cacheValue, String cacheKey) {
            this.cacheUtilLockAbstract = cacheUtilLockAbstract;
            this.cacheManager = cacheManager;
            this.cacheValue = cacheValue;
            this.cacheKey = cacheKey;
            this.lockKey = "AvoidBreakdownBuilder.lock:" + cacheValue + "_" + cacheKey;
        }

        public AvoidBreakdownBuilder setLockKey(String lockKey) {
            this.lockKey = lockKey;
            return this;
        }

        public AvoidBreakdownBuilder setExpire(long expire) {
            this.expire = expire;
            return this;
        }

        public AvoidBreakdownBuilder setSleepMillis(long sleepMillis) {
            this.sleepMillis = sleepMillis;
            return this;
        }

        private AvoidBreakdownBuilder setUseSelfCacheable(boolean useSelfCacheable) {
            isUseSelfCacheable = useSelfCacheable;
            return this;
        }

        public CacheUtil2 build() {
            return new CacheUtil2(this);
        }
    }

    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public @interface Cacheable {
        //缓存区域，默认类名+方法名
        @AliasFor("cacheNames")
        String value() default "";

        //缓存区域，默认类名+方法名
        @AliasFor("value")
        String cacheNames() default "";

        //缓存区域下的键，默认SimpleKeyGenerator方式
        String key() default "";

        //锁的键，默认："AvoidBreakdownBuilder.lock:" + cacheValue + "_" + cacheKey
        String lockKey() default "";

        //锁失效时间，毫秒值
        long expire() default 5000;

        //拿锁失败睡眠时间，毫秒值，默认90-120随机数
        long sleepMillis() default -1;

        //使用自定义cacheManager
        String cacheManager() default "";

        //缓存过期时间,毫秒值
        //如果不传,缓存过期时间为默认cacheManager的配置时间,
        //如果传了自定义的cacheManager,则不能使用该时间,如此时传入会抛出异常
        long aliveMillis() default -1;
    }

    @Component
    public static class SpringContextUtils implements ApplicationContextAware {
        private ApplicationContext applicationContext = null;

        private SpringContextUtils() {
        }

        @Override
        public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
            this.applicationContext = applicationContext;
        }

        public ApplicationContext getApplicationContext() {
            Assert.notNull(applicationContext, "applicationContext must not be null");
            return applicationContext;
        }

        public <T> T getBean(String beanName, Class<T> requiredType) {
            Assert.notNull(applicationContext, "applicationContext must not be null");
            T bean;
            try {
                bean = applicationContext.getBean(beanName, requiredType);
            } catch (BeansException e) {
                return null;
            }
            return bean;
        }

        public Object getBean(String beanName) {
            Assert.notNull(applicationContext, "applicationContext must not be null");
            Object bean;
            try {
                bean = applicationContext.getBean(beanName);
            } catch (BeansException e) {
                return null;
            }
            return bean;
        }

        public synchronized <T> T setBean(String beanName, Class<T> clazz) {
            return setBean(beanName, clazz, null);
        }

        public synchronized <T> T setBean(String beanName, Class<T> clazz, Map<String, Object> original) {
            DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
            if (beanFactory.containsBean(beanName)) {
                return beanFactory.getBean(beanName, clazz);
            }
            GenericBeanDefinition definition = new GenericBeanDefinition();
            //类class
            definition.setBeanClass(clazz);
            //属性赋值
            if (original != null) {
                definition.setPropertyValues(new MutablePropertyValues(original));
            }
            //注册到spring上下文
            beanFactory.registerBeanDefinition(beanName, definition);
            return beanFactory.getBean(beanName, clazz);
        }

        public String getActiveProfile() {
            Assert.notNull(applicationContext, "applicationContext must not be null");
            return applicationContext.getEnvironment().getActiveProfiles()[0];
        }

    }

    /**
     * 使用aop配合注解实现缓存防击穿获取
     * 1被注解标记的方法在执行前获取缓存，有缓存就返回
     * 2没有缓存尝试拿锁，抢到锁后交给本方法执行
     */
    @Aspect
    @Component
    public static class CacheAop {
        @Autowired
        private CacheUtilLockAbstract cacheUtilLockAbstract;
        @Autowired
        private CacheManager cacheManager;
        @Autowired(required = false)
        private KeyGenerator keyGenerator;
        private DefaultParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();
        private ExpressionParser parser = new SpelExpressionParser();
        @Autowired
        private SpringContextUtils springContextUtils;
        @Autowired
        private RedisConnectionFactory redisConnectionFactory;
        @Autowired
        DefaultListableBeanFactory defaultListableBeanFactory;

        @Around("@annotation(cacheable)")
        public Object Around(ProceedingJoinPoint point, Cacheable cacheable) throws Throwable {
            MethodSignature signature = (MethodSignature) point.getSignature();
            //拦截的方法名
            String methodName = signature.getName();
            //拦截的实体类
            Object target = point.getTarget();
            //拦截的方法的实际参数值数组
            Object[] arguments = point.getArgs();
            //拦截的方法参数对象
            Method method = signature.getMethod();
            //拦截的方法名数组
            String[] params = discoverer.getParameterNames(method);
            //获取缓存区域名
            String value = cacheable.value();
            if (value.equals(""))
                value = target.getClass().getSimpleName() + "_" + methodName;
            String key = cacheable.key();
            key = generateKey(target, method, arguments, params, key);
            //获取缓存失效时间
            long aliveMillis = cacheable.aliveMillis();
            if (aliveMillis < 0 && aliveMillis != -1) {
                throw new RuntimeException("time不允许小于0!");
            }
            //初始化cacheManager
            String customCacheManagerName = cacheable.cacheManager();
            CacheManager targetCacheManager = getCacheManager(key, aliveMillis, customCacheManagerName);
            AvoidBreakdownBuilder avoidPenetrationBuilder =
                    new AvoidBreakdownBuilder(cacheUtilLockAbstract, targetCacheManager, value, key)
                            .setExpire(cacheable.expire())
                            .setUseSelfCacheable(true);
            if (cacheable.sleepMillis() != -1)
                avoidPenetrationBuilder.setSleepMillis(cacheable.sleepMillis());
            if (!cacheable.lockKey().equals(""))
                avoidPenetrationBuilder.setLockKey(cacheable.lockKey());
            CacheUtil2 build = avoidPenetrationBuilder.build();
            return build.avoidBreakdown(point::proceed);
        }

        private CacheManager getCacheManager(String key, long aliveMillis, String customCacheManagerName) {
            CacheManager targetCacheManager;//判断缓存时间,如果time有值并且customCacheManager无值,则使用新的cacheManager
            if (aliveMillis != -1) {
                if (!customCacheManagerName.equals("")) {
                    throw new RuntimeException("time与自定义cacheManager不可同时存在!");
                } else {
                    CacheManager newCacheManager = springContextUtils.getBean(key, CacheManager.class);
                    if (newCacheManager == null) {
                        //没有则根据key重新创建cacheManager,并放到ioc中
                        targetCacheManager = RedisCacheManager
                                .builder(RedisCacheWriter.nonLockingRedisCacheWriter(redisConnectionFactory))
                                .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMillis(aliveMillis)))
                                .build();
                        defaultListableBeanFactory.registerSingleton(key, targetCacheManager);
                    } else {
                        targetCacheManager = newCacheManager;
                    }
                }
            } else {
                //time有没有值,如果没传自定义cacheManager则使用默认配置的
                if (customCacheManagerName.equals("")) {
                    targetCacheManager = cacheManager;
                } else {
                    targetCacheManager = springContextUtils.getBean(customCacheManagerName, CacheManager.class);
                }
            }
            return targetCacheManager;
        }

        private String generateKey(Object target, Method method, Object[] arguments, String[] params, String key) {
            if (key.equals("")) {
                //key没有传并且未配置KeyGenerator，则使用官方方式
                if (keyGenerator == null) {
                    //如果方法没有参数，则使用0作为key
                    //如果只有一个参数的话则使用该参数作为key
                    //如果参数多余一个的话则使用所有参数的hashCode作为key
                    return SimpleKeyGenerator.generateKey(arguments).toString();
                } else {
                    return keyGenerator.generate(target, method, arguments).toString();
                }

            } else {
                //有形参的用形参实际值，如果没有参数，则直接使用key
                if (params != null && params.length != 0) {
                    EvaluationContext context = new StandardEvaluationContext();
                    for (int len = 0; len < params.length; len++) {
                        context.setVariable(params[len], arguments[len]);
                    }
                    //解析表达式并获取spel的值
                    Expression expression = parser.parseExpression(key);
                    Object value1 = expression.getValue(context);
                    if (value1 != null) {
                        key = value1.toString();
                    }
                } else {
                    return key;
                }
            }
            return key;
        }

    }
}
