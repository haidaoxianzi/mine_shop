package com.ms.order.openfeign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @Auther: gina
 * @Date: 2025-03-07
 * @Description:
 */
@FeignClient(name = "product-module")
public interface ProductService {

    @GetMapping("/product/{productId}")
    public String getProduct(@PathVariable Integer productId);

}
