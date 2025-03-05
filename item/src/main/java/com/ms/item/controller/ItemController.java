package com.ms.item.controller;

import com.ms.item.exception.BusinessException;
import com.ms.item.model.ItemModel;
import com.ms.item.response.CommonReturnType;
import com.ms.item.service.ItemService;
import com.ms.item.vo.ItemVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Controller
@RequestMapping("/item")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Value("${rocketmq.producer.topic}")
    private String topic;

    //创建商品的controller
    @RequestMapping(value = "/create", method = {RequestMethod.POST})
    @ResponseBody
    public CommonReturnType createItem(@RequestParam(name = "title") String title,
                                       @RequestParam(name = "description") String description,
                                       @RequestParam(name = "price") BigDecimal price,
                                       @RequestParam(name = "stock") Integer stock,
                                       @RequestParam(name = "imgUrl") String imgUrl) throws BusinessException {
        //封装service请求用来创建商品
        ItemModel itemModel = new ItemModel();
        itemModel.setTitle(title);
        itemModel.setDescription(description);
        itemModel.setPrice(price);
        itemModel.setStock(stock);
        itemModel.setImgUrl(imgUrl);

        ItemModel itemModelForReturn = itemService.createItem(itemModel);
        ItemVO itemVO = convertVOFromModel(itemModelForReturn);

        return CommonReturnType.create(itemVO);
    }

   /* ExecutorService es = new ThreadPoolExecutor(10, 10, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<>(10), new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r);
        }
    }, new ThreadPoolExecutor.AbortPolicy());*/

    ScheduledExecutorService esService = Executors.newScheduledThreadPool(1);

    //更新商品
    @RequestMapping(value = "/update", method = {RequestMethod.POST})
    @ResponseBody
    public CommonReturnType updateItem(@RequestBody ItemModel itemModel) throws BusinessException {
        String key = "item_" + itemModel.getId();

        //方式一，通过线程池，完成异步双删策略
        //step1、 删除缓存
       /* redisTemplate.delete(key);
        //step2、更新数据库
        ItemModel itemModelForReturn = itemService.updateItem(itemModel);
        ItemVO itemVO = convertVOFromModel(itemModelForReturn);
        //step3、异步处理
        esService.schedule(() -> {
           log.info("再次删除缓存");
            redisTemplate.delete(key);
        }, 1, TimeUnit.SECONDS);
       */

        //方式二 更新之后直接发mq的形式更新/删除缓存
        ItemModel itemModelForReturn = itemService.updateItem(itemModel);
        ItemVO itemVO = convertVOFromModel(itemModelForReturn);

        //rocketMQTemplate.convertAndSend(topic, itemModel.getId());
        log.info("发送商品更新ID到mq,id: {}", itemModel.getId());
        return CommonReturnType.create(itemVO);
    }

    //商品详情优化前
    //@RequestMapping(value = "/get", method = {RequestMethod.GET})
    @ResponseBody
    public CommonReturnType getItem2(@RequestParam(name = "id") Integer id) {
        ItemModel itemModel = itemService.getItemById(id);
        ItemVO itemVO = convertVOFromModel(itemModel);
        return CommonReturnType.create(itemVO);
    }

    //商品详情页浏览
    @RequestMapping(value = "/get", method = {RequestMethod.GET})
    @ResponseBody
    public CommonReturnType getItem(@RequestParam(name = "id") Integer id) {
        ItemModel itemModel = ItemModel.toBean((String) redisTemplate.opsForValue().get("item_" + id));
        System.out.println("查缓存结果：" + itemModel);
        //todo 123wq 并发量较大时，考虑到缓存没值，首次查询会有多个线程争抢，这里要加锁
       /* if (null == itemModel) {
            itemModel = itemService.getItemById(id);
            redisTemplate.opsForValue().set("item_" + id, ItemModel.toJsonString(itemModel));
            System.out.println("查库，存储 缓存");
        }*/
        ItemVO itemVO = convertVOFromModel(itemModel);
        return CommonReturnType.create(itemVO);
    }


    private ItemVO convertVOFromModel(ItemModel itemModel) {
        if (itemModel == null) {
            return null;
        }
        ItemVO itemVO = new ItemVO();
        BeanUtils.copyProperties(itemModel, itemVO);
        if (itemModel.getPromoModel() != null) {
            //有正在进行或即将进行的秒杀活动
            itemVO.setPromoStatus(itemModel.getPromoModel().getStatus());
            itemVO.setPromoId(itemModel.getPromoModel().getId());
            itemVO.setStartDate(itemModel.getPromoModel().getStartDate().toString(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")));
            itemVO.setPromoPrice(itemModel.getPromoModel().getPromoItemPrice());
        } else {
            itemVO.setPromoStatus(0);
        }
        return itemVO;
    }
}
