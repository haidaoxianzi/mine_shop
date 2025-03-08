package com.ms.order.openfeign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @Auther: gina
 * @Date: 2025-03-07
 * @Description:
 */
@FeignClient(name = "stock_module")
public interface StockFeignClient {
    @GetMapping("/stock/reduce/{productId}")
    public String reduce(@PathVariable Integer productId);
}
