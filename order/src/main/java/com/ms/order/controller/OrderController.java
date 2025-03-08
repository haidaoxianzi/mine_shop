package com.ms.order.controller;

import com.ms.order.openfeign.ProductService;
import com.ms.order.openfeign.ShopcartService;
import com.ms.order.openfeign.StockFeignClient;
import com.ms.order.openfeign.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @Auther: gina
 * @Date: 2025-03-07
 * @Description:
 */
@RestController
@Slf4j
public class OrderController {
    @Autowired
    private ProductService productService;

    @Autowired
    private ShopcartService shopcartService;

    @Autowired
    private UserService userService;

    @Autowired
    private StockFeignClient stockFeignClient;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @GetMapping("/order/create")
    public String createOrder(Integer productId, Integer userId) throws InterruptedException, ExecutionException {
        long start = System.currentTimeMillis();
        String userName = userService.getUserName(userId);
        String productName = productService.getProduct(productId);
        String isStockReduce = stockFeignClient.reduce(productId);
        String isShopcartRemove = shopcartService.remove(productId, userId);

        long end1 = System.currentTimeMillis();
        log.info("用户{} ,购买商品‘{}’ ,库存扣减情况：{} ,购物车移除情况：{} ", userName, productName, isStockReduce, isShopcartRemove);
        log.info("优化前执行耗时：{}", end1 - start);

        rocketMQTemplate.convertAndSend("ms-create-order",userId);

/*

        ExecutorService es = Executors.newFixedThreadPool(4);
        List<Callable<String>> tasks = new ArrayList<>();
        tasks.add(() -> {
            return userService.getUserName(userId);
        });
        tasks.add(() -> {
            return productService.getProduct(productId);
        });
        tasks.add(() -> {
            return stockFeignClient.reduce(productId);
        });

        tasks.add(() -> {
            return shopcartService.remove(productId, userId);
        });

        List<Future<String>> futures = es.invokeAll(tasks);
        userName = futures.get(0).get();
        productName = futures.get(1).get();
        isStockReduce = futures.get(2).get();
        isShopcartRemove = futures.get(3).get();
        long end2 = System.currentTimeMillis();
        log.info("用户{} ,购买商品‘{}’ ,库存扣减情况：{} ,购物车移除情况：{} ", userName, productName, isStockReduce, isShopcartRemove);
        log.info("基于Callable任务列表+线程池-----优化后，耗时:{}", end2 - end1);

        ExecutorService es2 = Executors.newFixedThreadPool(3);
        CompletableFuture<String> userFuture = CompletableFuture.supplyAsync(() -> {
            return userService.getUserName(userId);
        }, es2);
        CompletableFuture<String> productFuture = CompletableFuture.supplyAsync(() -> {
            return productService.getProduct(productId);
        }, es2);

        CompletableFuture<String> stockFuture = CompletableFuture.supplyAsync(() -> {
            return stockFeignClient.reduce(productId);
        }, es2);

        CompletableFuture<String> cartFuture = CompletableFuture.supplyAsync(() -> {
            return shopcartService.remove(productId, userId);
        }, es2);

        userName = userFuture.get();
        productName = productFuture.get();
        isStockReduce = stockFuture.get();
        isShopcartRemove = cartFuture.get();

        long end3 = System.currentTimeMillis();
        log.info("用户{} ,购买商品‘{}’ ,库存扣减情况：{} ,购物车移除情况：{} ", userName, productName, isStockReduce, isShopcartRemove);
        log.info("基于Completable的任务编排+线程池-----优化后，耗时:{}", end3 - end2);
*/

        return "SUCCESS";
    }

}
