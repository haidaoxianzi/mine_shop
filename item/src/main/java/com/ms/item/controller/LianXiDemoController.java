package com.ms.item.controller;

import com.ms.item.exception.BusinessException;
import com.ms.item.model.ItemModel;
import com.ms.item.response.CommonReturnType;
import com.ms.item.service.ItemService;
import com.ms.item.vo.ItemVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.joda.time.format.DateTimeFormat;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/item1")
public class LianXiDemoController {

    @Autowired
    private ItemService itemService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Value("${rocketmq.producer.topic}")
    private String topic;

    @Autowired
    private RedissonClient redissonClient;

    RBloomFilter<String> bloomFilter;

    RBloomFilter<String> hotDataBloomFilter;

    @PostConstruct
    public void init() {
        //todo 这里要注意，除了初始化时把全部值加载到布隆过滤器，在添加，删除元素时，也要维护，步骤省略哈
        //创建布隆过滤器
        bloomFilter = redissonClient.getBloomFilter("items-bloomfilter");
        //初始化布隆过滤器，预计初始的元素个数10000，误差率0.03
        bloomFilter.tryInit(100000, 0.03);

        //热点数据布隆过滤器
        hotDataBloomFilter = redissonClient.getBloomFilter("hots-bloomFilter");
        hotDataBloomFilter.tryInit(100000, 0.03);
    }

    @GetMapping("/addBloom/{id}")
    public String addHotBloom(@PathVariable("id") Integer id) {
        hotDataBloomFilter.add("item_" + id);
        return "ok";
    }

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
        bloomFilter.add("item_" + itemModel.getId());

        return CommonReturnType.create(itemVO);
    }


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
        String key = "item_" + id;
        ItemModel itemModel = ItemModel.toBean((String) redisTemplate.opsForValue().get(key));
        System.out.println("查缓存结果：" + itemModel);

        //解决缓存穿透问题方案二：使用布隆过滤器防止缓存穿透
        if (!bloomFilter.contains(key)) {
            log.info("布隆过滤器判断没有对应数据,要获取的数据itemId:{}", key);
            return CommonReturnType.create(null);
        }
        if (null != itemModel) {
            ItemVO itemVO = convertVOFromModel(itemModel);
            return CommonReturnType.create(itemVO);
        }
        //如果是热点数据，解决缓存击穿问题方案二：加互斥锁
        if (hotDataBloomFilter.contains(key)) {
            return dealByLock(id);
        }
        //todo 123wq 并发量较大时，考虑到缓存没值，首次查询会有多个线程争抢，这里要加锁
        if (null == itemModel) {
            itemModel = itemService.getItemById(id);
            //热点数据，解决缓存击穿问题方案一：缓存数据永不过期[不设置过期时间，直接返回]
            if (hotDataBloomFilter.contains(key)) {

                redisTemplate.opsForValue().set(key, ItemModel.toJsonString(itemModel));
                ItemVO itemVO = convertVOFromModel(itemModel);
                return CommonReturnType.create(itemVO);
            }

            redisTemplate.opsForValue().set(key, ItemModel.toJsonString(itemModel), 10, TimeUnit.SECONDS);
            //解决缓存穿透问题方案一：缓存存储空值并设置过期时间
            if (itemModel == null) {
                redisTemplate.opsForValue().set(key, null, 5, TimeUnit.SECONDS);
            }
            System.out.println("查库，存储 缓存");
        }
        ItemVO itemVO = convertVOFromModel(itemModel);
        return CommonReturnType.create(itemVO);
    }

    /**
     * 如果是热点数据，解决缓存击穿问题方案二：
     * 加互斥锁
     */
    private CommonReturnType dealByLock(Integer id) {
        String key = "item_" + id;
        String lockKey = "item-hots-deals";
        RLock lock = redissonClient.getLock(key);
        ItemModel itemModel = null;
        try {
            lock.lock();
            itemModel = itemService.getItemById(id);
            if (null == itemModel) {
                redisTemplate.opsForValue().set(key, ItemModel.toJsonString(itemModel), 10, TimeUnit.SECONDS);
            } else {
                redisTemplate.opsForValue().set(key, null, 5, TimeUnit.SECONDS);
            }
        } finally {
            lock.unlock();
        }
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

    //解决缓存击穿和穿透方案落地 Demo1
    //商品详情页浏览
    @RequestMapping(value = "/get3", method = {RequestMethod.GET})
    @ResponseBody
    public CommonReturnType getItem3(@RequestParam(name = "id") Integer id) {
        String key = "item_" + id;
        //解决缓存穿透问题方案二：使用布隆过滤器防止缓存穿透
        if (!bloomFilter.contains(key)) {
            log.info("布隆过滤器判断没有对应数据,要获取的数据itemId:{}", key);
            return CommonReturnType.create(null);
        }
        ItemModel itemModel = ItemModel.toBean((String) redisTemplate.opsForValue().get(key));
        System.out.println("查缓存结果：" + itemModel);
        //todo 123wq 并发量较大时，考虑到缓存没值，首次查询会有多个线程争抢，这里要加锁
        if (null == itemModel) {
            itemModel = itemService.getItemById(id);
            //热点数据，解决缓存击穿问题方案一：缓存数据永不过期[不设置过期时间，直接返回]
            if (hotDataBloomFilter.contains(key)) {
                redisTemplate.opsForValue().set(key, ItemModel.toJsonString(itemModel));
                ItemVO itemVO = convertVOFromModel(itemModel);
                return CommonReturnType.create(itemVO);
            }
            redisTemplate.opsForValue().set(key, ItemModel.toJsonString(itemModel), 10, TimeUnit.SECONDS);
            System.out.println("查库，存储 缓存");
        }
        ItemVO itemVO = convertVOFromModel(itemModel);
        return CommonReturnType.create(itemVO);
    }

    //解决缓存击穿和穿透方案落地 Demo2
    //商品详情页浏览
    @RequestMapping(value = "/get4", method = {RequestMethod.GET})
    @ResponseBody
    public CommonReturnType getItem4(@RequestParam(name = "id") Integer id) {
        String key = "item_" + id;
        ItemModel itemModel = ItemModel.toBean((String) redisTemplate.opsForValue().get(key));
        System.out.println("查缓存结果：" + itemModel);
        String lockKey = "item-hots-deals";
        //缓存失效要查库，就是要加互斥锁。【包括热点数据解决缓存击穿问题方案二：加互斥锁】
        RLock lock = redissonClient.getLock(lockKey);
        try {
            lock.lock();
            itemModel = itemService.getItemById(id);
            if (null != itemModel) {
                redisTemplate.opsForValue().set(key, ItemModel.toJsonString(itemModel), 10, TimeUnit.SECONDS);
            } else {
                redisTemplate.opsForValue().set(key, null, 5, TimeUnit.SECONDS);
            }
        } finally {
            lock.unlock();
        }
        ItemVO itemVO = convertVOFromModel(itemModel);
        return CommonReturnType.create(itemVO);
    }
}
