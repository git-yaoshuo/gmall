package com.yaoshuo.gmall.service.payment;

import com.yaoshuo.gmall.bean.payment.PaymentInfo;
import com.yaoshuo.gmall.enums.PaymentStatus;

import java.util.Map;

public interface PaymentService {

    /**
     * 根据orderId获取一个与之对应的paymentInfo对象
     * @param orderId
     */
    PaymentInfo getPaymentInfoByOrderId(String orderId);

    /**
     * 获取支付订单信息
     * @param paymentInfoQuery
     * @return
     */
    PaymentInfo getPaymentInfo(PaymentInfo paymentInfoQuery);

    /**
     * 修改支付订单状态
     * @param out_trade_no
     * @param paymentInfoUpd
     */
    void updatePaymentInfo(String out_trade_no, PaymentInfo paymentInfoUpd);

    /**
     * 支付订单
     * @param paymentInfo
     * @return
     */
    String payOrder(PaymentInfo paymentInfo);

    /**
     * 异步回调
     * @param paramMap
     */
    String paymentNotify(Map<String, String> paramMap);

    /**
     * 退款
     * @param orderId
     * @return
     */
    boolean refund(String orderId);

    /**
     * 支付后发送结果给订单模块，发送消息到activemq
     * @param paymentInfo
     * @param result
     */
    void sendPaymentResult(PaymentInfo paymentInfo, String result);

    /**
     * 检查订单是否已支付
     * @param paymentInfo
     * @return
     */
    boolean checkPaymentInfo(PaymentInfo paymentInfo);

    /**
     * 使用延迟队列反复检查订单是否已支付完成
     * @param outTradeNo 订单第三方交易编号
     * @param delaySeconds 延迟确认时间===秒
     * @param checkCount 总共检查的次数
     */
    void sendDelayPaymentResult(String outTradeNo, int delaySeconds, int checkCount);

    /**
     *修改支付订单的状态为关闭
     * @param orderId
     */
    void closePayment(String orderId);
}
