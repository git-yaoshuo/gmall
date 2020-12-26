package com.yaoshuo.gmall.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.yaoshuo.gmall.bean.payment.PaymentInfo;
import com.yaoshuo.gmall.annotation.LoginCheckAnnotation;
import com.yaoshuo.gmall.bean.order.OrderInfo;
import com.yaoshuo.gmall.service.order.OrderService;
import com.yaoshuo.gmall.service.payment.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Slf4j
@Controller
public class PaymentController {

    @Reference
    private PaymentService paymentService;

    @Reference
    private OrderService orderService;

    /**
     * 退款
     * @return
     */
    @ResponseBody
    @PostMapping("/alipay/refund")
    @LoginCheckAnnotation
    public String refund(String orderId){

        boolean flag = paymentService.refund(orderId);

        if(flag){
            System.out.println("退款成功");
        } else {
            System.out.println("退款失败");
        }
       return flag + "";
    }

    /**
     * 根据订单orderId，查询订单是否已支付
     * @param paymentInfo
     * @return
     */
    @ResponseBody
    @RequestMapping("/alipay/query")
    public String query(PaymentInfo paymentInfo){
        boolean flag = paymentService.checkPaymentInfo(paymentInfo);
        return flag ? "已支付" : "未支付";
    }

    /**
     * 根据订单id，使用支付宝付款支付
     * @param orderId
     * @return
     */
    @ResponseBody
    @PostMapping("/alipay/submit")
    @LoginCheckAnnotation
    public String aliPaySubmit(String orderId, HttpServletResponse response){

        //保存支付订单信息,先根据orderId获取一个与之对应的paymentInfo对象
        PaymentInfo paymentInfo = paymentService.getPaymentInfoByOrderId(orderId);

        String result = paymentService.payOrder(paymentInfo);
        response.setContentType("text/html;charset=UTF-8");

        //设置延迟队列，反复确认订单是否已支付
        paymentService.sendDelayPaymentResult(paymentInfo.getOutTradeNo(), 15, 3);

        return result;
    }

    /**
     * 同步回调
     * @return
     */
    @RequestMapping(value = "/alipay/callback/return",method = RequestMethod.GET)
    public String callbackReturn(){
        return "redirect:http://cart.gmall.com/cartList";
    }

    /**
     * 异步回调
     * @param paramMap
     * @return
     */
    @RequestMapping(value = "/alipay/callback/notify",method = RequestMethod.POST)
    @ResponseBody
    public String paymentNotify(@RequestParam Map<String,String> paramMap ) {
        String result = paymentService.paymentNotify(paramMap);

        return  result;
    }

    /**
     * 测试支付完成后向订单模块发送消息到消息中间件activemq中
     * @param paymentInfo
     * @param result
     */
    @ResponseBody
    @RequestMapping("/sendPaymentResult")
    public void sendPaymentResult(PaymentInfo paymentInfo, String result){
        paymentService.sendPaymentResult(paymentInfo, result);
    }

    /**
     * 到结算页面中
     * @param orderId
     * @param request
     * @return
     */
    @RequestMapping("/toPay")
    @LoginCheckAnnotation
    public String toPayPage(String orderId, HttpServletRequest request){

        OrderInfo orderInfo = orderService.getOrderInfoByOrderId(orderId);

        request.setAttribute("orderId", orderId);
        request.setAttribute("totalAmount", orderInfo.getTotalAmount());

        return "index";
    }

}
