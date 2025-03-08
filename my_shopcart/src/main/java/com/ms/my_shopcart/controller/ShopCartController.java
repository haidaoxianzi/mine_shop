package com.ms.my_shopcart.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Auther: gina
 * @Date: 2025-03-07
 * @Description:
 */
@RestController
public class ShopCartController {

    @GetMapping("/shopcart/remove/{productId}/{userId}")
    public String remove(@PathVariable Integer productId,@PathVariable Integer userId){
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return "SUCCESS";
    }
}
