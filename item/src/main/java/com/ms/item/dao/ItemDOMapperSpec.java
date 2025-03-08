package com.ms.item.dao;


import com.ms.item.dao.data.ItemDO;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface ItemDOMapperSpec {

    List<ItemDO> getItemsByTime(Date beforeTime);

    Integer countRecords();

    List<ItemDO> selectDataByCons(@Param("offset") Integer startPos, @Param("limit") int pageSize);

    List<ItemDO> getItemsByBatch(@Param("startId") Integer startId,@Param("endId") Integer endId);
}