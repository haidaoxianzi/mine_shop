package com.ms.order.openfeign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @Auther: gina
 * @Date: 2025-03-07
 * @Description:
 */
@FeignClient(name="shopcart_module")
public interface ShopcartService {

    @GetMapping("/shopcart/remove/{productId}/{userId}")
    public String remove(@PathVariable Integer productId,@PathVariable Integer userId);

}
