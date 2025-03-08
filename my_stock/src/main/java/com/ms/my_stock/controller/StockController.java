package com.ms.my_stock.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Auther: gina
 * @Date: 2025-03-07
 * @Description:
 */
@RestController
@Slf4j
public class StockController {

    @GetMapping("/stock/reduce/{productId}")
    public String reduce(@PathVariable Integer productId) {
        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("已扣减库存");
        return "SUCCESS";
    }
}
