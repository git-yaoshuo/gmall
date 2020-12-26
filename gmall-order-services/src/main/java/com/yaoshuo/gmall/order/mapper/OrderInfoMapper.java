package com.yaoshuo.gmall.order.mapper;

import com.yaoshuo.gmall.bean.order.OrderInfo;
import tk.mybatis.mapper.common.Mapper;

public interface OrderInfoMapper extends Mapper<OrderInfo> {

    /**
     * 根据订单id获取订单信息
     * @param orderId
     * @return
     */
    OrderInfo getOrderInfoByOrderId(String orderId);
}
