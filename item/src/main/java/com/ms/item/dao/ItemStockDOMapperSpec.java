package com.ms.item.dao;

import com.ms.item.dao.data.ItemStockDO;

public interface ItemStockDOMapperSpec {

    ItemStockDO selectByItemId(Integer itemId);

    void updateStockByItemId(ItemStockDO itemStockDO);
}