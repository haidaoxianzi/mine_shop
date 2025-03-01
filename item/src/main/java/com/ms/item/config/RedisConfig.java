package com.ms.item.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.ms.item.serializer.DateTimeJsonDeserializer;
import com.ms.item.serializer.DateTimeJsonSerializer;
import org.joda.time.DateTime;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

/**
 * @Auther: gina
 * @Date: 2025-02-26
 * @Description:
 */
@Component
public class RedisConfig {
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        StringRedisSerializer strRedisSerializer = new StringRedisSerializer();
        //序列化key值
        redisTemplate.setKeySerializer(strRedisSerializer);

        //解决value的序列化方式
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);

        ObjectMapper objectMapper=new ObjectMapper();

        SimpleModule simpleModule=new SimpleModule();
        simpleModule.addSerializer(DateTime.class, new DateTimeJsonSerializer());
        simpleModule.addDeserializer(DateTime.class, new DateTimeJsonDeserializer());

        objectMapper.registerModule(simpleModule);

        /*
        mapper.registerModule(new JodaModule());
        */

        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        return redisTemplate;
    }
}
