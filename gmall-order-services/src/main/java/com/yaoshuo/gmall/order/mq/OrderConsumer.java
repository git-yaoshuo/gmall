package com.yaoshuo.gmall.order.mq;

import com.alibaba.dubbo.config.annotation.Reference;
import com.yaoshuo.gmall.enums.ProcessStatus;
import com.yaoshuo.gmall.service.order.OrderService;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;

@Component
public class OrderConsumer {

    @Reference
    private OrderService orderService;

    @JmsListener(destination = "PAYMENT_RESULT_QUEUE",containerFactory = "jmsListenerFactory")
    public void consumerPaymentResult(MapMessage mapMessage) {

        try {
            String orderId = mapMessage.getString("orderId");
            String result = mapMessage.getString("result");

            System.err.println("orderId ==" + orderId);
            System.err.println("result ==" + result);

            //支付成功
            if ("success".equals(result)){

                //支付成功后修改订单状态为====已支付
                orderService.updateOrderInfoStatus(orderId, ProcessStatus.PAID);

                //通知库存系统减库存
                orderService.sendOrderStatus(orderId);

                //修改订单状态为已通知库存系统
                orderService.updateOrderInfoStatus(orderId, ProcessStatus.NOTIFIED_WARE);

            }

        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

    @JmsListener(destination = "SKU_DEDUCT_QUEUE",containerFactory = "jmsListenerFactory")
    public void consumerSkuDeduct(MapMessage mapMessage) {

        try {
            String orderId = mapMessage.getString("orderId");
            String taskStatus = mapMessage.getString("taskStatus");

            System.err.println("orderId ==" + orderId);
            System.err.println("taskStatus ==" + taskStatus);

            //支付成功
            if ("DEDUCTED".equals(taskStatus) ){
                //修改订单状态为已通知库存系统
                orderService.updateOrderInfoStatus(orderId, ProcessStatus.WAITING_DELEVER);
            }else {
                orderService.updateOrderInfoStatus(orderId, ProcessStatus.STOCK_EXCEPTION);
            }

        } catch (JMSException e) {
            e.printStackTrace();
        }

    }
}
