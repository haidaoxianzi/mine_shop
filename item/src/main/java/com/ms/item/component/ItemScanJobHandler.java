package com.ms.item.component;

import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Auther: gina
 * @Date: 2025-03-03
 * @Description:
 */
@Component
public class ItemScanJobHandler {
    @Autowired
    private ItemScanSchedule itemScanSchedule;

    @XxlJob("ItemScanJobHandler")
    public ReturnT<String> itemScanJobHandler(String jobParam) throws Exception {
        itemScanSchedule.scanItemSynNew();
        return ReturnT.SUCCESS;
    }
}
