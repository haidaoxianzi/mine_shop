package com.ms.order.openfeign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @Auther: gina
 * @Date: 2025-03-07
 * @Description:
 */
@FeignClient(name="user_module")
public interface UserService {

    @GetMapping("/user/{userId}")
    String getUserName(@PathVariable Integer UserId);


}
