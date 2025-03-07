package com.ms.item.service.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.ms.item.service.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * @Auther: gina
 * @Date: 2025-03-07
 * @Description:本地缓存配置类
 */
@Component
public class CacheServiceImpl implements CacheService {
    private Cache<String, Object> commonCache = null;
    @PostConstruct
    public void init() {
        commonCache = CacheBuilder.newBuilder().
                //设置过期时间
                expireAfterAccess(60, TimeUnit.SECONDS)
                //设置缓存中初始容量和最大可存储容量，超过100则按LRU策略移除缓存项
                .initialCapacity(10).maximumSize(100).build();
    }
    @Override
    public void setCommonCache(String str, Object obj) {
        commonCache.put(str,obj);
    }

    @Override
    public Object getCommonCache(String str) {
        return commonCache.getIfPresent(str);
    }
}
