package com.yaoshuo.gmall.order.task;

import com.alibaba.dubbo.config.annotation.Reference;
import com.yaoshuo.gmall.bean.order.OrderInfo;
import com.yaoshuo.gmall.service.order.OrderService;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@EnableScheduling
@Component
public class OrderTask {

    @Reference
    private OrderService orderService;

    @Scheduled(cron = "0/20 * * * * ?")
    public void checkOrder(){

        List<OrderInfo> expireOrderList = orderService.getExpireOrderList();

        if (expireOrderList != null && expireOrderList.size() > 0){
            for (OrderInfo orderInfo : expireOrderList) {

                //处理过期未支付订单
                orderService.execExpireOrder(orderInfo);
            }
        }

    }
}
