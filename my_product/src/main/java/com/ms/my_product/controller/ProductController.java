package com.ms.my_product.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Auther: gina
 * @Date: 2025-03-07
 * @Description:
 */
@RestController
public class ProductController {

    @GetMapping("product/{prodyctId}")
    public  String getProduct(@PathVariable Integer productId){
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
           e.printStackTrace();
        }
        return "宇宙超超超超～级好吃冻酸奶";
    }
}
