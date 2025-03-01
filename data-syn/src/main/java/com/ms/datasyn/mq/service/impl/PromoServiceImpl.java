package com.ms.datasyn.mq.service.impl;
import com.ms.datasyn.mq.dao.PromoDOMapper;
import com.ms.datasyn.mq.dao.PromoDOMapperSpec;
import com.ms.datasyn.mq.dao.data.PromoDO;
import com.ms.datasyn.mq.model.PromoModel;
import com.ms.datasyn.mq.service.PromoService;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PromoServiceImpl implements PromoService {

    @Autowired
    private PromoDOMapper promoDOMapper;
    @Autowired
    private PromoDOMapperSpec promoDOMapperSpec;

    @Override
    public PromoModel getPromoByItemId(Integer itemId) {

        PromoDO promoDO = promoDOMapperSpec.selectByItemId(itemId);


        PromoModel promoModel = convertFromDataObject(promoDO);
        if(promoModel == null){
            return null;
        }


        if(promoModel.getStartDate().isAfterNow()){
            promoModel.setStatus(1);
        }else if(promoModel.getEndDate().isBeforeNow()){
            promoModel.setStatus(3);
        }else{
            promoModel.setStatus(2);
        }
        return promoModel;
    }
    private PromoModel convertFromDataObject(PromoDO promoDO){
        if(promoDO == null){
            return null;
        }
        PromoModel promoModel = new PromoModel();
        BeanUtils.copyProperties(promoDO,promoModel);
        promoModel.setPromoItemPrice(new BigDecimal(promoDO.getPromoItemPrice()));
        promoModel.setStartDate(new DateTime(promoDO.getStartDate()));
        promoModel.setEndDate(new DateTime(promoDO.getEndDate()));
        return promoModel;
    }
}
