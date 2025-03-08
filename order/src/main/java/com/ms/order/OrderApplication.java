package com.ms.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class OrderApplication {
    // todo 123wq 这里卡在： 服务注册和 feignClien 调用
    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }

}
