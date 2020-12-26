package com.yaoshuo.gmall.payment.mq;

import com.alibaba.dubbo.config.annotation.Reference;
import com.yaoshuo.gmall.bean.payment.PaymentInfo;
import com.yaoshuo.gmall.service.payment.PaymentService;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;

@Component
public class PaymentConsumer {

    @Reference
    private PaymentService paymentService;

    @JmsListener(destination = "PAYMENT_RESULT_CHECK_QUEUE",containerFactory = "jmsListenerFactory")
    public void consumerCheckPaymentInfo(MapMessage mapMessage) {

        try {
            String outTradeNo = mapMessage.getString("outTradeNo");
            int delaySeconds = mapMessage.getInt("delaySeconds");
            int checkCount = mapMessage.getInt("checkCount");

            System.err.println("outTradeNo ==" + outTradeNo);
            System.err.println("delaySeconds ==" + delaySeconds);
            System.err.println("checkCount ==" + checkCount);

            PaymentInfo paymentInfoQuery = new PaymentInfo();

            paymentInfoQuery.setOutTradeNo(outTradeNo);
            PaymentInfo paymentInfo = paymentService.getPaymentInfo(paymentInfoQuery);


            boolean flag = paymentService.checkPaymentInfo(paymentInfo);

            if (flag){
                paymentService.sendPaymentResult(paymentInfo, "success");
            }

            //未支付，并且检查次数不足 checkCount 次，则继续重复延迟检查
            if (!flag && checkCount > 0){
                paymentService.sendDelayPaymentResult(outTradeNo, delaySeconds, checkCount - 1);
            }


        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

}
