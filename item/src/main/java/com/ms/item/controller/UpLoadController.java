package com.ms.item.controller;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
/**
 * 锁粒度调整
 * */
@RestController
public class UpLoadController {
    @Autowired
    private RedissonClient redissonClient;
    @RequestMapping("/doSave")
    public  synchronized void  doSave1(String path,String filterUrl){

        // 创建文件夹
        if(!existPath(path)){
            mkdir();
        }
        // 上传文件
        uploadFile(filterUrl);
        // 发送消息
        sendMessage(filterUrl);

        }
    // 更改单机锁的粒度
    @RequestMapping("/doSave2")
    public  synchronized void  doSave2(String path,String filterUrl){

        synchronized (this){// 创建文件夹
            if(!existPath(path)){
                mkdir();
            }
        }
        // 上传文件
        uploadFile(filterUrl);
        // 发送消息
        sendMessage(filterUrl);

        }
    //增加分布式锁
    @RequestMapping("/doSave3")
    public  void  doSave3(String path,String filterUrl) {
        String lockKey = "mkdir-lock";
        RLock lock = redissonClient.getLock(lockKey);
        if (lock.tryLock()) {
            try {
                // 创建文件夹
                if (!existPath(path)) {
                    mkdir();
                }
                // 上传文件
                uploadFile(filterUrl);
                // 发送消息
                sendMessage(filterUrl);
            } finally {
                lock.unlock();
                ;
            }
        }
    }

    @RequestMapping("/doSave6")
    public  void  doSave6(String path,String filterUrl){
        String lockKey = "mkdir-lock";
        RLock lock = redissonClient.getLock(lockKey);
        if(!lock.tryLock()) {
            return;
        }
        try{
            // 创建文件夹
            if(!existPath(path)){
                mkdir();
            }
        }finally {
            lock.unlock();;
        }
        // 上传文件
        uploadFile(filterUrl);
        // 发送消息
        sendMessage(filterUrl);

    }


    private boolean existPath(String path) {
        return true;
    }

    private void sendMessage(String filterUrl) {
    }

    private void uploadFile(String filterUrl) {
    }


    private void mkdir() {
    }
}
