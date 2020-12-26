package com.yaoshuo.gware.mq;

import com.alibaba.fastjson.JSON;

import com.yaoshuo.gware.bean.WareOrderTask;
import com.yaoshuo.gware.enums.TaskStatus;
import com.yaoshuo.gware.mapper.WareOrderTaskDetailMapper;
import com.yaoshuo.gware.mapper.WareOrderTaskMapper;
import com.yaoshuo.gware.mapper.WareSkuMapper;
import com.yaoshuo.gware.service.GwareService;

import com.yaoshuo.gware.config.ActiveMQUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.TextMessage;
import java.util.*;

/**
 * @param
 * @return
 */
@Component
public class WareConsumer {


    @Autowired
    WareOrderTaskMapper wareOrderTaskMapper;

    @Autowired
    WareOrderTaskDetailMapper wareOrderTaskDetailMapper;

    @Autowired
    WareSkuMapper wareSkuMapper;

    @Autowired
    ActiveMQUtil activeMQUtil;

    @Autowired
    JmsTemplate jmsTemplate;

    @Autowired
    GwareService gwareService;



    @JmsListener(destination = "ORDER_RESULT_QUEUE", containerFactory = "jmsQueueListener")
    public void receiveOrder(TextMessage textMessage) throws JMSException {

        String orderTaskJson = textMessage.getText();

        WareOrderTask wareOrderTask = JSON.parseObject(orderTaskJson, WareOrderTask.class);

        wareOrderTask.setTaskStatus(TaskStatus.PAID);
        gwareService.saveWareOrderTask(wareOrderTask);
        textMessage.acknowledge();

        List<WareOrderTask> wareSubOrderTaskList = gwareService.checkOrderSplit(wareOrderTask);
        if (wareSubOrderTaskList != null && wareSubOrderTaskList.size() >= 2) {
            for (WareOrderTask orderTask : wareSubOrderTaskList) {
                gwareService.lockStock(orderTask);
                //拆单的订单，向订单模块发送订单状态
                gwareService.sendSkuDeductStatus(orderTask.getOrderId());
            }
        } else {
            gwareService.lockStock(wareOrderTask);
            //不拆单的订单，向订单模块发送订单状态
            gwareService.sendSkuDeductStatus(wareOrderTask.getOrderId());
        }
    }

}
