package com.ms.datasyn.mq.model;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class PromoModel implements Serializable {
    private Integer id;

    //秒杀活动状态 1表示还未开始，2表示进行中，3表示已结束
    private Integer status;

    //秒杀活动名称
    private String promoName;

    //秒杀活动的开始时间
    private DateTime startDate;

    //秒杀活动的结束时间
    private DateTime endDate;

    //秒杀活动的适用商品
    private Integer itemId;

    //秒杀活动的商品价格
    private BigDecimal promoItemPrice;

    public static String toJsonString(PromoModel promoModel) {
        return JSON.toJSONString(promoModel);
    }

    public static PromoModel toBean(String jsonString) {
        return JSON.parseObject(jsonString,PromoModel.class);
    }
}
