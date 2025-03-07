package com.ms.item.service;

/**
 * @Auther: gina
 * @Date: 2025-03-07
 * @Description:本地缓存工具类
 */
public interface CacheService {
    /*存*/
    void setCommonCache(String str, Object obj);

    /*查*/
    Object getCommonCache(String str);
}
