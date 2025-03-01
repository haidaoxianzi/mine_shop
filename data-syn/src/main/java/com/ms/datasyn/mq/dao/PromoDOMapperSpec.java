package com.ms.datasyn.mq.dao;

import com.ms.datasyn.mq.dao.data.PromoDO;

public interface PromoDOMapperSpec {
    PromoDO selectByItemId(Integer itemId);
}