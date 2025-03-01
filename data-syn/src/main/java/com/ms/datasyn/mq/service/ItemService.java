package com.ms.datasyn.mq.service;

import com.ms.datasyn.mq.exception.BusinessException;
import com.ms.datasyn.mq.model.ItemModel;

public interface ItemService {

    //创建商品
    ItemModel createItem(ItemModel itemModel) throws BusinessException;

    //更新商品
    ItemModel updateItem(ItemModel itemModel) throws BusinessException;


    //商品详情浏览
    ItemModel getItemById(Integer id);


}
