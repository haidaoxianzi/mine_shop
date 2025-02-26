package com.ms.item.dao;

import com.ms.item.dao.data.PromoDO;

public interface PromoDOMapperSpec {
    PromoDO selectByItemId(Integer itemId);
}