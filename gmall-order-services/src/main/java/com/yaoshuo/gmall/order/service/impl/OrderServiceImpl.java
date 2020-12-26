package com.yaoshuo.gmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.yaoshuo.gmall.bean.order.OrderDetail;
import com.yaoshuo.gmall.bean.order.OrderInfo;
import com.yaoshuo.gmall.enums.ProcessStatus;
import com.yaoshuo.gmall.order.mapper.OrderDetailMapper;
import com.yaoshuo.gmall.order.mapper.OrderInfoMapper;
import com.yaoshuo.gmall.service.order.OrderService;
import com.yaoshuo.gmall.service.payment.PaymentService;
import com.yaoshuo.gmall.util.ActivemqUtil;
import com.yaoshuo.gmall.util.JedisUtils;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;
import com.yaoshuo.gmall.common.util.HttpClientUtil;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import javax.jms.Queue;
import java.util.*;

import static com.yaoshuo.gmall.constant.OrderConstant.OUT_TRADE_NO_PREFIX;
import static com.yaoshuo.gmall.constant.RedisConstant.*;
import static com.yaoshuo.gmall.enums.OrderStatus.UNPAID;

@Service
public class OrderServiceImpl implements OrderService {

    @Reference
    private PaymentService paymentService;

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    /**
     * 根据商品的skuId和skuNum校验是否有库存
     * @param skuId
     * @param skuNum
     * @return
     */
    @Override
    public boolean verifyStock(String skuId, Integer skuNum) {

        //校验是否有库存=====远程使用httpclients 调用gware-manage服务
        String hasStockUrl = "http://www.gware.com/hasStock?skuId="+ skuId +"&num=" + skuNum;
        String result = HttpClientUtil.doGet(hasStockUrl);

        return "1".equals(result);
    }

    /**
     * 保存订单，并返回订单id
     * @param orderInfo
     * @return
     */
    @Transactional
    @Override
    public String saveOrder(OrderInfo orderInfo) {

        //设置订单号
        String outTradeNo = OUT_TRADE_NO_PREFIX + System.currentTimeMillis() + new Random().nextInt(10000);
        orderInfo.setOutTradeNo(outTradeNo);

        //设置创建时间
        orderInfo.setCreateTime(new Date());

        //设置订单过期时间====设置为一天
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 1);
        orderInfo.setExpireTime(calendar.getTime());

        //保存订单信息
        orderInfoMapper.insertSelective(orderInfo);

        String orderId = orderInfo.getId();

        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        if (orderDetailList != null && orderDetailList.size() > 0){
            for (OrderDetail orderDetail : orderDetailList) {

                orderDetail.setOrderId(orderId);
                orderDetailMapper.insertSelective(orderDetail);
            }
        }

        //删除流水订单号
        deleteTradeNo(orderInfo.getUserId());

        return orderId;
    }

    /**
     * 获取订单流水号，并保存到redis中
     * @param userId
     * @return
     */
    @Override
    public String getTradeNo(String userId) {

        Jedis jedis = null;

        try {
            jedis = JedisUtils.getJedis();
            String tradeNoKey = REDIS_PREFIX_ORDER + userId + REDIS_SUFFIX_ORDER_TRADE_NO;

            String tradeNo = UUID.randomUUID().toString().replace("-", "");
            jedis.setex(tradeNoKey, REDIS_USER_LOGIN_TIME_OUT, tradeNo);

            return tradeNo;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }finally {
            if (jedis != null){
                jedis.close();
            }
        }
    }

    /**
     * 校验流水订单号是否一致
     * @param tradeNo
     * @param userId
     * @return
     */
    @Override
    public boolean verifyTradeNo(String tradeNo, String userId) {

        Jedis jedis = null;

        try {
            jedis = JedisUtils.getJedis();

            String tradeNoKey = REDIS_PREFIX_ORDER + userId + REDIS_SUFFIX_ORDER_TRADE_NO;
            String tradeNoFromRedis = jedis.get(tradeNoKey);

           return tradeNo.equals(tradeNoFromRedis);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }finally {
            if (jedis != null){
                jedis.close();
            }
        }
    }

    /**
     * 删除redis中的流水订单号
     * @param userId
     */
    @Override
    public void deleteTradeNo(String userId) {
        Jedis jedis = null;

        try {
            jedis = JedisUtils.getJedis();

            String tradeNoKey = REDIS_PREFIX_ORDER + userId + REDIS_SUFFIX_ORDER_TRADE_NO;
            jedis.del(tradeNoKey);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (jedis != null){
                jedis.close();
            }
        }
    }

    /**
     * 根据订单id获取订单信息
     * @param orderId
     * @return
     */
    @Override
    public OrderInfo getOrderInfoByOrderId(String orderId) {

        return orderInfoMapper.getOrderInfoByOrderId(orderId);
    }

    /**
     * 根据orderId修改订单支付状态
     * @param orderId
     * @param paid
     */
    @Transactional
    @Override
    public void updateOrderInfoStatus(String orderId, ProcessStatus paid) {

        OrderInfo orderInfo = new OrderInfo();

        orderInfo.setId(orderId);
        orderInfo.setOrderStatus(paid.getOrderStatus());
        orderInfo.setProcessStatus(paid);

        orderInfoMapper.updateByPrimaryKeySelective(orderInfo);
    }

    /**
     * 支付成功后，订单状态修改后，通知库存系统减库存，将消息发送到activemq消息中间件中
     * @param orderId
     */
    @Override
    public void sendOrderStatus(String orderId) {

        Connection connection = null;
        Session session = null;
        MessageProducer producer = null;

        try {
            connection = ActivemqUtil.getConnection();
            connection.start();

            session = connection.createSession(true, Session.SESSION_TRANSACTED);

            Queue queue = session.createQueue("ORDER_RESULT_QUEUE");
            producer = session.createProducer(queue);

            ActiveMQTextMessage activeMQTextMessage = new ActiveMQTextMessage();

            //获取一个orderInfo的json串，封装订单信息
            OrderInfo orderInfo = getOrderInfoByOrderId(orderId);
            Map<String, Object> orderInfoMap = convertOrderInfoToMap(orderInfo);
            String orderInfoJson = JSON.toJSONString(orderInfoMap);

            activeMQTextMessage.setText(orderInfoJson);

            producer.send(activeMQTextMessage);
            session.commit();

        } catch (JMSException e) {
            e.printStackTrace();
        }finally {
            ActivemqUtil.close(producer, session, connection);
        }
    }

    /**
     * 获取过期的订单列表
     * @return
     */
    @Override
    public List<OrderInfo> getExpireOrderList() {

        Example example = new Example(OrderInfo.class);

        example.createCriteria()
                .andEqualTo("processStatus", ProcessStatus.UNPAID)
                .andLessThan("expireTime", new Date());

        return orderInfoMapper.selectByExample(example);
    }

    /**
     * 处理过期的或者为完成的订单
     * @param orderInfo
     */
    @Override
    public void execExpireOrder(OrderInfo orderInfo) {

        //修改订单的状态为已关闭
        updateOrderInfoStatus(orderInfo.getId(), ProcessStatus.CLOSED);

        //修改支付订单的状态为已关闭
        paymentService.closePayment(orderInfo.getId());
    }

    /**
     * 将一个orderInfo对象转化为一个map对象
     * @param orderInfo
     * @return
     */
    public Map<String,Object> convertOrderInfoToMap(OrderInfo orderInfo) {

        HashMap<String, Object> orderInfoMap = new HashMap<>();

        orderInfoMap.put("orderId", orderInfo.getId());
        orderInfoMap.put("consignee", orderInfo.getConsignee());
        orderInfoMap.put("consigneeTel", orderInfo.getConsigneeTel());
        orderInfoMap.put("orderComment", orderInfo.getOrderComment());
        orderInfoMap.put("orderBody", orderInfo.getTradeBody());
        orderInfoMap.put("deliveryAddress", orderInfo.getDeliveryAddress());
        orderInfoMap.put("paymentWay", "2");
        orderInfoMap.put("wareId",orderInfo.getWareId());

        List<Map<String,Object>> detailList = new ArrayList<>();

        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        if (orderDetailList != null && orderDetailList.size() > 0){

            for (OrderDetail orderDetail : orderDetailList) {

                HashMap<String, Object> orderDetailMap = new HashMap<>();

                orderDetailMap.put("skuId",orderDetail.getSkuId());
                orderDetailMap.put("skuNum",orderDetail.getSkuNum());
                orderDetailMap.put("skuName",orderDetail.getSkuName());

                detailList.add(orderDetailMap);
            }
        }
        orderInfoMap.put("details", detailList);

        return orderInfoMap;
    }

    /**
     * 拆分订单
     * @param orderId
     * @param wareSkuMap
     * @return
     */
    @Override
    public List<OrderInfo> splitOrder(String orderId, String wareSkuMap) {

        //根据orderId获取原始订单
        OrderInfo orderInfo = getOrderInfoByOrderId(orderId);
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();

        //反序列化wareSkuMap为：封装了wareId和skuIds的map对象的一个list集合
        List<Map> mapList = JSON.parseArray(wareSkuMap, Map.class);

        ArrayList<OrderInfo> subOrderInfoList = new ArrayList<>();

        if (mapList != null && mapList.size() > 0){
            for (Map map : mapList) {

                String wareId = (String) map.get("wareId");
                List<String> skuIds = (List<String>) map.get("skuIds");

                //创建一个子订单，其内容，除了订单id和订单价格，第三方交易编号outTradeNo，以及parentOrderId外，其他内容与主订单一致
                OrderInfo subOrderInfo = new OrderInfo();

                BeanUtils.copyProperties(orderInfo, subOrderInfo);

                subOrderInfo.setId(null);
                subOrderInfo.setParentOrderId(orderInfo.getId());
                subOrderInfo.setWareId(wareId);

                List<OrderDetail> subOrderDetailList = new ArrayList<>();

                for (String skuId : skuIds) {

                    if (orderDetailList != null && orderDetailList.size() > 0){
                        for (OrderDetail orderDetail : orderDetailList) {

                            if (skuId.equals(orderDetail.getSkuId())){
                               orderDetail.setId(null);
                               subOrderDetailList.add(orderDetail);
                               break;
                            }
                        }
                    }
                }
                //设置子订单的订单详情
                subOrderInfo.setOrderDetailList(subOrderDetailList);
                subOrderInfo.sumTotalAmount();

                //保存子订单
                saveOrder(subOrderInfo);

                subOrderInfoList.add(subOrderInfo);
            }
        }

        updateOrderInfoStatus(orderId, ProcessStatus.SPLIT);

        return subOrderInfoList;
    }
}
