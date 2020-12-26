package com.yaoshuo.gmall.bean.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
public class UpdateOrderStatusVo implements Serializable {

    private String auth_app_id;

    private String app_id;//支付宝支付的id

    private String seller_id;//卖家id
    private String buyer_id;//买家id

    private String out_trade_no;//商品订单号
    private String trade_no; //支付宝交易号
    private String subject;//商品名称
    private String total_amount;//交易金额
    private String invoice_amount;//发票金额
    private String buyer_pay_amount;//买家支付金额
    private String trade_status; //交易状态

    private String sign_type;//数字签名类型
    private String sign;//数字签名值

    private String notify_id;//通知id
    private String notify_type;//通知类型

    private  Map<String,String> params = new HashMap<String,String>();

    private String gmt_create;
    private String charset;
    private String gmt_payment;
    private String notify_time;
    private String version;
    private String fund_bill_list;
    private String receipt_amount;
    private String point_amount;

}
