package com.ms.datasyn.mq.config;

/**
 * @Auther: gina
 * @Date: 2025-02-28
 * @Description:
 */
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;

public class JacksonConfig {
    public static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JodaModule()); // 注册Joda-Time模块
        return mapper;
    }
}
