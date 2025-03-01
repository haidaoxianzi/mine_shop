package com.ms.datasyn;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.ms.datasyn.mq.dao")
public class DataSynApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataSynApplication.class, args);
    }

}
