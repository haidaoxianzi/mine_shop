package com.ms.item.component;

import com.alibaba.fastjson.JSON;
import com.ms.item.dao.ItemDOMapperSpec;
import com.ms.item.dao.data.ItemDO;
import com.ms.item.model.ItemModel;
import com.ms.item.service.ItemService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.xxl.job.core.util.ShardingUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.tomcat.util.threads.ThreadPoolExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.*;

/**
 * @Auther: gina
 * @Date: 2025-03-03
 * @Description:
 */
@Component
@Slf4j
public class ShardingItemScanJobHandler {

    @Autowired
    private ItemDOMapperSpec itemDOMapperSpec;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ItemService itemService;

    ThreadPoolExecutor tpe = new ThreadPoolExecutor(3, 3, 1, TimeUnit.SECONDS, new LinkedBlockingDeque<>(), new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            return new Thread("item-info-fresh");
        }
    }, new ThreadPoolExecutor.AbortPolicy());

    @XxlJob("ShardingItemScanJobHandler")
    public ReturnT<String> shardingJobHandler(String jobParam) {
        ShardingUtil.ShardingVO shardingVo = ShardingUtil.getShardingVo();
        int totalSharding = shardingVo.getTotal();
        int index = shardingVo.getIndex();
        log.info("分片配置信息：当前分片序号/总分片数 == {}/{}", index, totalSharding);
        //获取所有数据条数
        //eg:14片，分成4份: 4，4，4，2
        try {
            //查库表总条数
            Integer totalCount = itemDOMapperSpec.countRecords();
            boolean isTotalCountLess = totalCount < totalSharding;
            log.info("ShardingItemScanJobHandler:: totalCount={},isTotalCountLess={}", totalCount, isTotalCountLess);
            //对业务数据进行分片
            Integer perShardingSize = isTotalCountLess ? totalCount : totalCount / totalSharding;
            Integer perShardingInitPos = isTotalCountLess ? 0 : index * perShardingSize;
            Integer endPos = isTotalCountLess ? totalCount : perShardingInitPos + perShardingSize;
            int pageSize = isTotalCountLess ? totalCount : 100;
            if (index == (totalSharding - 1) & isTotalCountLess) {
                endPos = totalCount;
            }
            log.info("ShardingItemScanJobHandler:: perShardingSize={},perShardingInitPos={},endPos={}", perShardingSize, perShardingInitPos, endPos);

            for (; perShardingInitPos <= endPos; perShardingInitPos += pageSize) {
                //各个分片，以100条为单位进行查询
                List<ItemDO> items = itemDOMapperSpec.selectDataByCons(perShardingInitPos, pageSize);
                if (CollectionUtils.isEmpty(items)) {
                    log.info("分片获取数据为空");
                    return ReturnT.SUCCESS;
                }
                log.info("分片获取数据 items.size={}", items.size());

                for (ItemDO item : items) {
                    ItemModel itemModel = itemService.getItemById(item.getId());
                    log.info("ShardingItemScanJobHandler::向redis同步 ");
                    // 查询之后，刷新redis,todo 这里可以对比item更新时间>缓存，则刷新
                    redisTemplate.opsForValue().set("item_" + item.getId(), JSON.toJSONString(itemModel));
                }

            }
        } catch (Exception e) {
            log.error("ShardingItemScanJobHandler:: {}", e.getMessage(), e);
        }

        return ReturnT.SUCCESS;
    }
}
