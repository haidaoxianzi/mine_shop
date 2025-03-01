package com.ms.datasyn.mq.component;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSONObject;
import com.ms.datasyn.mq.model.ItemModel;
import com.ms.datasyn.mq.service.ItemService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @Auther: gina
 * @Date: 2025-03-01
 * @Description:
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = "pro-item-topic", consumerGroup = "item-update-group")
public class ItemUpdateConsumer implements RocketMQListener<String> {

    @Autowired
    private ItemService itemService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void onMessage(String itemId) {

        if (StringUtils.isEmpty(itemId)) {
            return;
        }
        log.info("item消费者监听到id={}的商品发生变动，同步刷新缓存", itemId);
        ItemModel itemModel = itemService.getItemById(Integer.valueOf(itemId));
        redisTemplate.opsForValue().set("item_" + itemId, JSONObject.toJSONString(itemModel),10, TimeUnit.MINUTES);

    }


}
