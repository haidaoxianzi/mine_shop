package com.ms.item.component;

import com.ms.item.dao.ItemDOMapperSpec;
import com.ms.item.dao.data.ItemDO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Auther: gina
 * @Date: 2025-03-02
 * @Description:定时任务：扫描商品信息/10s，刷新缓存
 */

@Slf4j
@Component
public class ItemScanSchedule {

    @Autowired
    private ItemDOMapperSpec itemDOMapperSpec;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Autowired
    private RedissonClient redissonClient;

    public void scanItemSynNew() {
        //1、查10s内更新的商品
        long beforeTimeVal = new Date().getTime() - 10000;
        Date beforeTime = new Date(beforeTimeVal);

        List<ItemDO> items = itemDOMapperSpec.getItemsByTime(beforeTime);
        if (CollectionUtils.isEmpty(items)) {
            log.info("ItemScanSchedule:: items is empty");
            return;
        }
        log.info("ItemScanSchedule:: items.size={}", items.size());
        //2、推送消息，刷新缓存中商品信息
        for (ItemDO vo : items) {
            rocketMQTemplate.convertAndSend("pro-item-topic", vo.getId());
        }

    }

    //@Scheduled(fixedDelay = 10000)
    //多台机器，需要加分布式锁
    public void scanItemSyn() {
        //1、查10s内更新的商品
        long beforeTimeVal = new Date().getTime() - 10000;
        Date beforeTime = new Date(beforeTimeVal);
        String lockKey = "item-scan-before-15";
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (!lock.tryLock(10, TimeUnit.MICROSECONDS)) {
                log.info("获取锁失败，失败");
                return;
            }
            List<ItemDO> items = itemDOMapperSpec.getItemsByTime(beforeTime);
            if (CollectionUtils.isEmpty(items)) {
                log.info("ItemScanSchedule:: items is empty");
                return;
            }
            log.info("ItemScanSchedule:: items.size={}", items.size());
            //2、推送消息，刷新缓存中商品信息
            for (ItemDO vo : items) {
                rocketMQTemplate.convertAndSend("pro-item-topic", vo.getId());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //释放锁
            log.info("释放锁");
            lock.unlock();
        }
    }
}
