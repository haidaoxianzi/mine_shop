package com.ms.datasyn.mq.dao;

import com.ms.datasyn.mq.dao.data.ItemStockDO;

public interface ItemStockDOMapperSpec {

    ItemStockDO selectByItemId(Integer itemId);

    void updateStockByItemId(ItemStockDO itemStockDO);
}