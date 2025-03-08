package com.ms.item.dao;

import com.ms.item.dao.data.ItemStockDO;

import java.util.List;

public interface ItemStockDOMapperSpec {

    ItemStockDO selectByItemId(Integer itemId);

    void updateStockByItemId(ItemStockDO itemStockDO);

    List<ItemStockDO> selectByItemIds(List<Integer> itemIds);
}