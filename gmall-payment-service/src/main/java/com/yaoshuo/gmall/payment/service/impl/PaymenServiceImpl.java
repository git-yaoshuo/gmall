package com.yaoshuo.gmall.payment.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.yaoshuo.gmall.bean.order.OrderInfo;
import com.yaoshuo.gmall.bean.payment.PaymentInfo;
import com.yaoshuo.gmall.enums.PaymentStatus;
import com.yaoshuo.gmall.payment.config.AlipayConfig;
import com.yaoshuo.gmall.payment.mapper.PaymentInfoMapper;
import com.yaoshuo.gmall.service.order.OrderService;
import com.yaoshuo.gmall.service.payment.PaymentService;
import com.yaoshuo.gmall.util.ActivemqUtil;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.yaoshuo.gmall.enums.PaymentStatus.*;
import static com.yaoshuo.gmall.enums.PaymentStatus.PAID;

@Service
public class PaymenServiceImpl implements PaymentService {

    @Autowired
    private PaymentInfoMapper paymentInfoMapper;

    @Reference
    private OrderService orderService;

    //获得初始化的AlipayClient
    AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.gatewayUrl, AlipayConfig.app_id, AlipayConfig.merchant_private_key, "json", AlipayConfig.charset, AlipayConfig.alipay_public_key, AlipayConfig.sign_type);

    /**
     * 获取支付订单信息
     * @param paymentInfoQuery
     * @return
     */
    @Override
    public PaymentInfo getPaymentInfo(PaymentInfo paymentInfoQuery) {
        return paymentInfoMapper.selectOne(paymentInfoQuery);
    }

    /**
     * 修改支付订单状态
     * @param out_trade_no
     * @param paymentInfoUpd
     */
    @Override
    public void updatePaymentInfo(String out_trade_no, PaymentInfo paymentInfoUpd) {

        Example example = new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("outTradeNo", out_trade_no);

        paymentInfoMapper.updateByExampleSelective(paymentInfoUpd, example);
    }

    /**
     * 保存支付信息，并支付订单
     * @param paymentInfo
     * @return
     */
    @Transactional
    @Override
    public String payOrder(PaymentInfo paymentInfo) {

        paymentInfoMapper.insertSelective(paymentInfo);

        //支付订单

        //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(AlipayConfig.return_url);
        alipayRequest.setNotifyUrl(AlipayConfig.notify_url);

        HashMap<String, Object> paramMap = new HashMap<>();

        paramMap.put("out_trade_no", paymentInfo.getOutTradeNo());
        paramMap.put("total_amount", paymentInfo.getTotalAmount());
        paramMap.put("subject", paymentInfo.getSubject());
        paramMap.put("body", paymentInfo.getSubject());
        paramMap.put("product_code", "FAST_INSTANT_TRADE_PAY");

        alipayRequest.setBizContent(JSON.toJSONString(paramMap));

        /*alipayRequest.setBizContent("{\"out_trade_no\":\""+ paymentInfo.getOutTradeNo() +"\","
                + "\"total_amount\":\""+ paymentInfo.getTotalAmount() +"\","
                + "\"subject\":\""+ paymentInfo.getSubject() +"\","
                + "\"body\":\""+ paymentInfo.getSubject() +"\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");*/

        //请求
        String result = "";
        try {
            result = alipayClient.pageExecute(alipayRequest).getBody();
            System.err.println(result);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 异步回调
     * @param paramMap
     */
    @Override
    public String paymentNotify(Map<String, String> paramMap) {
        System.err.println(paramMap);
        boolean signVerified = false;
        try {
            signVerified = AlipaySignature.rsaCheckV1(paramMap, AlipayConfig.alipay_public_key, AlipayConfig.charset, AlipayConfig.sign_type); //调用SDK验证签名
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if(signVerified){
            // TODO 验签成功后，按照支付结果异步通知中的描述，对支付结果中的业务内容进行二次校验，校验成功后在response中返回success并继续商户自身业务处理，校验失败返回failure
            //校验支付订单out_trade_no是否存在
            String out_trade_no = paramMap.get("out_trade_no");
            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setOutTradeNo(out_trade_no);

            PaymentInfo paymentInfoExists = paymentInfoMapper.selectOne(paymentInfo);

            if (paymentInfoExists == null){
                return "failure";
            }

            //校验支付总金额是否一致
            String total_amount = paramMap.get("total_amount");
            BigDecimal totalAmount = paymentInfoExists.getTotalAmount();

            if (totalAmount.compareTo(BigDecimal.valueOf(Double.parseDouble(total_amount))) != 0){
                return "failure";
            }

            //修改订单交易状态
            String trade_status = paramMap.get("trade_status");
            if ("TRADE_SUCCESS".equals(trade_status) || "TRADE_FINISHED".equals(trade_status)){

                // 修改
                if (paymentInfoExists.getPaymentStatus() == UNPAID){

                    //修改订单交易状态为完成
                    PaymentInfo paymentInfoUpd = new PaymentInfo();
                    // 设置状态
                    paymentInfoUpd.setPaymentStatus(PAID);
                    paymentInfoUpd.setAlipayTradeNo(paramMap.get("trade_no"));
                    // 设置创建时间
                    paymentInfoUpd.setCallbackTime(new Date());
                    // 设置内容
                    paymentInfoUpd.setCallbackContent(paramMap.toString());
                    updatePaymentInfo(out_trade_no, paymentInfoUpd);


                }else {
                    return "failure";
                }
            }

            return "success";
        }else{
            // TODO 验签失败则记录异常日志，并在response中返回failure.
            return "failure";
        }
    }

    /**
     * TODO 退款======有点问题，暂不能用
     * @param orderId
     * @return
     */
    @Override
    public boolean refund(String orderId) {

        PaymentInfo paymentInfo = getPaymentInfoByOrderId(orderId);

        AlipayTradeRefundRequest alipayRequest = new AlipayTradeRefundRequest();

        HashMap<String, Object> map = new HashMap<>();

        map.put("trade_no",paymentInfo.getAlipayTradeNo());
        map.put("out_trade_no", paymentInfo.getOutTradeNo());
        map.put("refund_amount", paymentInfo.getTotalAmount());
        map.put("refund_reason", "正常退款");

        alipayRequest.setApiVersion(JSON.toJSONString(map));

       /* alipayRequest.setBizContent("{\"out_trade_no\":\""+ paymentInfo.getOutTradeNo() +"\","
                + "\"trade_no\":\""+ paymentInfo.getAlipayTradeNo() +"\","
                + "\"refund_amount\":\""+ paymentInfo.getTotalAmount() +"\","
                + "\"refund_reason\":\"正常退款\"," + "\"}");*/

        //请求
        boolean flag = false;
        try {
            flag = alipayClient.execute(alipayRequest).isSuccess();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return flag;
    }


    /**
     * 支付后发送结果给订单模块，发送消息到activemq
     * @param paymentInfo
     * @param result
     */
    @Override
    public void sendPaymentResult(PaymentInfo paymentInfo, String result) {

        Connection connection = null;
        Session session = null;
        MessageProducer producer = null;

        try {
            connection = ActivemqUtil.getConnection();
            connection.start();

            session = connection.createSession(true, Session.CLIENT_ACKNOWLEDGE);

            Queue queue = session.createQueue("PAYMENT_RESULT_QUEUE");
            producer = session.createProducer(queue);

            ActiveMQMapMessage activeMQMapMessage = new ActiveMQMapMessage();

            activeMQMapMessage.setString("orderId", paymentInfo.getOrderId());
            activeMQMapMessage.setString("result", result);

            producer.send(activeMQMapMessage);
            session.commit();

        } catch (JMSException e) {
            e.printStackTrace();
        }finally {
            ActivemqUtil.close(producer, session, connection);
        }

    }

    /**
     * 检查订单是否已支付
     * @param paymentInfoQuery
     * @return
     */
    @Override
    public boolean checkPaymentInfo(PaymentInfo paymentInfoQuery) {

        PaymentInfo paymentInfo = getPaymentInfo(paymentInfoQuery);

        if (paymentInfo.getPaymentStatus() == PAID || paymentInfo.getPaymentStatus() == ClOSED){
            return true;
        }

        //调用支付宝查询接口查询订单是否已完成支付
//        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
//
//        HashMap<String, Object> paramMap = new HashMap<>();
//
//        paramMap.put("out_trade_no", paymentInfo.getOutTradeNo());
//        paramMap.put("trade_no", paymentInfo.getAlipayTradeNo());
//
//        request.setBizContent(JSON.toJSONString(paramMap));
//
//        /*request.setBizContent("{" +
//                "\"out_trade_no\":\"20150320010101001\"," +
//                "\"trade_no\":\"2014112611001004680 073956707\"," +
//                "\"org_pid\":\"2088101117952222\"," +
//                "      \"query_options\":[" +
//                "        \"trade_settle_info\"" +
//                "      ]" +
//                "  }");*/
//
//        AlipayTradeQueryResponse response = null;
//        try {
//            response = alipayClient.execute(request);
//        } catch (AlipayApiException e) {
//            e.printStackTrace();
//        }
//        if(response != null && response.isSuccess()){
//            System.out.println("调用成功");
//        } else {
//            System.out.println("调用失败");
//        }

        return false;
    }

    /**
     * 使用延迟队列反复检查订单是否已支付完成
     * @param outTradeNo 订单第三方交易编号
     * @param delaySeconds 延迟确认时间===秒
     * @param checkCount 总共检查的次数
     */
    @Override
    public void sendDelayPaymentResult(String outTradeNo, int delaySeconds, int checkCount) {

        Connection connection = null;
        Session session = null;
        MessageProducer producer = null;
        try {
            connection = ActivemqUtil.getConnection();
            connection.start();

            session = connection.createSession(true, Session.SESSION_TRANSACTED);

            Queue queue = session.createQueue("PAYMENT_RESULT_CHECK_QUEUE");
            producer = session.createProducer(queue);

            ActiveMQMapMessage activeMQMapMessage = new ActiveMQMapMessage();

            activeMQMapMessage.setString("outTradeNo", outTradeNo);
            activeMQMapMessage.setInt("delaySeconds", delaySeconds);
            activeMQMapMessage.setInt("checkCount", checkCount);

            //设置延迟检查时间 ===== 设置的是毫秒
            activeMQMapMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY, delaySeconds * 1000L);

            producer.send(activeMQMapMessage);
            session.commit();

        } catch (JMSException e) {
            e.printStackTrace();
        }finally {
            ActivemqUtil.close(producer, session, connection);
        }
    }

    /**
     * 修改支付订单的状态为关闭
     * @param orderId
     */
    @Async //利用多线程实现异步并发操作
    @Override
    public void closePayment(String orderId) {

        Example example = new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("orderId", orderId);

        PaymentInfo paymentInfoQuery = new PaymentInfo();
        paymentInfoQuery.setPaymentStatus(ClOSED);

        paymentInfoMapper.updateByExampleSelective(paymentInfoQuery, example);
    }


    /**
     * 根据orderId获取一个与之对应的paymentInfo对象
     * @param orderId
     * @return
     */
    public PaymentInfo getPaymentInfoByOrderId(String orderId) {

        OrderInfo orderInfo = orderService.getOrderInfoByOrderId(orderId);

        PaymentInfo paymentInfo = new PaymentInfo();

        paymentInfo.setOrderId(orderId);
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setTotalAmount(orderInfo.getTotalAmount());
        paymentInfo.setSubject(orderInfo.getOrderComment());
        paymentInfo.setPaymentStatus(UNPAID);
        paymentInfo.setCreateTime(new Date());

        return paymentInfo;
    }

}
