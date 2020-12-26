package com.yaoshuo.gmall.service.order;

import com.yaoshuo.gmall.bean.order.OrderInfo;
import com.yaoshuo.gmall.enums.OrderStatus;
import com.yaoshuo.gmall.enums.ProcessStatus;

import java.util.List;
import java.util.Map;

public interface OrderService {

    /**
     * 根据商品的skuId和skuNum校验是否有库存
     * @param skuId
     * @param skuNum
     * @return
     */
    boolean verifyStock(String skuId, Integer skuNum);

    /**
     * 保存订单，并返回订单id
     * @param orderInfo
     * @return
     */
    String saveOrder(OrderInfo orderInfo);

    /**
     * 获取一个流水订单号
     * @return
     */
    String getTradeNo(String userId);

    /**
     * 校验流水订单号是否一致
     * @param tradeNo
     * @param userId
     * @return
     */
    boolean verifyTradeNo(String tradeNo, String userId);

    /**
     * 删除redis中的订单流水号
     * @param userId
     */
    void deleteTradeNo(String userId);

    /**
     * 根据订单id查询订单信息
     * @param orderId
     * @return
     */
    OrderInfo getOrderInfoByOrderId(String orderId);

    /**
     * 根据orderId修改订单支付状态
     * @param orderId
     * @param paid
     */
    void updateOrderInfoStatus(String orderId,ProcessStatus paid);

    /**
     * 支付成功后，订单状态修改后，通知库存系统减库存，将消息发送到activemq消息中间件中
     * @param orderId
     */
    void sendOrderStatus(String orderId);

    /**
     * 获取所有过期的订单列表
     * @return
     */
    List<OrderInfo> getExpireOrderList();

    /**
     * 处理过期的订单
     * @param orderInfo
     */
    void execExpireOrder(OrderInfo orderInfo);

    /**
     * 将一个orderInfo对象转化为一个map对象
     * @param orderInfo
     * @return
     */
    Map<String,Object> convertOrderInfoToMap(OrderInfo orderInfo);

    /**
     * 拆分订单
     * @param orderId
     * @param wareSkuMap
     * @return
     */
    List<OrderInfo> splitOrder(String orderId, String wareSkuMap);
}
