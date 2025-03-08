package com.ms.usermodel.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Auther: gina
 * @Date: 2025-03-07
 * @Description:
 */
@RestController
public class UserController {

    @GetMapping("/user/{userId}")
    public String getUserName(@PathVariable Integer userId){
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
           e.printStackTrace();
        }
        return "Nancy";
    }
}
