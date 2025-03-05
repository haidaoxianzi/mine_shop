package com.ms.my_canal.component;

import com.ms.my_canal.bean.Item;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.javatool.canal.client.annotation.CanalTable;
import top.javatool.canal.client.handler.EntryHandler;

/**
 * @Auther: gina
 * @Date: 2025-03-04
 * @Description:
 */
@Slf4j
@Component
@CanalTable(value = "item")
public class ItemHandler implements EntryHandler<Item> {
    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    public void insert(Item t) {
        log.info("ItemHandler::新增数据 item {}", t);
        rocketMQTemplate.convertAndSend("pro-item-topic",t.getId());
    }

    public void update(Item before, Item after) {
        log.info("ItemHandler::修改数据 before: {},after:{}",before,after );
        rocketMQTemplate.convertAndSend("pro-item-topic",after.getId());

    }

    public void delete(Item t) {
        log.info("ItemHandler::删除数据 Item: {}",t);
        rocketMQTemplate.convertAndSend("pro-item-topic",t.getId());

    }
}
