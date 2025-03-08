package com.ms.msg_push.component;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * @Auther: gina
 * @Date: 2025-03-08
 * @Description:
 */
@Slf4j
@Component
@RocketMQMessageListener(topic="ms-create-order",consumerGroup = "order_msg_push")
public class OrderConsumer implements RocketMQListener<String> {
    @Override
    public void onMessage(String userId) {
        log.info("给用户 {} 推送购买商品成功短信",userId);
    }
}
