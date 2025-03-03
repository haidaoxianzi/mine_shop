package com.ms.item.dao;


import com.ms.item.dao.data.ItemDO;

import java.util.Date;
import java.util.List;

public interface ItemDOMapperSpec {

    List<ItemDO> getItemsByTime(Date beforeTime);
}