package com.yaoshuo.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.yaoshuo.gmall.annotation.LoginCheckAnnotation;
import com.yaoshuo.gmall.bean.cart.CartInfo;
import com.yaoshuo.gmall.bean.manage.SkuInfo;
import com.yaoshuo.gmall.bean.order.OrderDetail;
import com.yaoshuo.gmall.bean.order.OrderInfo;
import com.yaoshuo.gmall.bean.user.UserAddress;
import com.yaoshuo.gmall.enums.ProcessStatus;
import com.yaoshuo.gmall.service.cart.CartService;
import com.yaoshuo.gmall.service.manage.SkuManageService;
import com.yaoshuo.gmall.service.order.OrderService;
import com.yaoshuo.gmall.service.user.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;

import static com.yaoshuo.gmall.constant.WebConst.ORDER_SUBMIT_REPEAT;
import static com.yaoshuo.gmall.constant.WebConst.SKU_UNDER_STOCK;
import static com.yaoshuo.gmall.enums.OrderStatus.UNPAID;

@Controller
@CrossOrigin
public class OrderController {

    //使用dubbo远程调用service服务，使用@Reference注解
    @Reference
    private UserService userService;

    @Reference
    private CartService cartService;

    @Reference
    private OrderService orderService;

    @Reference
    private SkuManageService skuManageService;

    /**
     * 根据订单id完成拆单====拆单依据：仓库不同，商家不同，物流渠道不同
     * @param orderId
     * @param wareSkuMap 封装了仓库wareId与skuId的一个map ==> list集合 ==> json字符串形式
     * @return 返回一个拆单后的子订单的list集合的 json字符串形式
     */
    @RequestMapping("/orderSplit")
    @ResponseBody
    public String orderSplit(String orderId, String wareSkuMap){

        List<OrderInfo> subOrderInfoList = orderService.splitOrder(orderId, wareSkuMap);

        ArrayList<Map<String, Object>> subOrderInfoMapList = new ArrayList<>();
        if (subOrderInfoList != null && subOrderInfoList.size() > 0){

            for (OrderInfo subOrderInfo : subOrderInfoList) {
                Map<String, Object> subOrderInfoMap = orderService.convertOrderInfoToMap(subOrderInfo);
                subOrderInfoMapList.add(subOrderInfoMap);
            }
        }

        return JSON.toJSONString(subOrderInfoMapList);
    }

    /**
     * 提交订单，保订单信息
     * @param orderInfo
     * @param tradeNo
     * @param request
     * @return
     */
    @RequestMapping("/submitOrder")
    @LoginCheckAnnotation
    public String submitOrder(OrderInfo orderInfo, String tradeNo, HttpServletRequest request){

        //初始化订单参数信息
        //设置订单默认初始值
        orderInfo.setOrderStatus(UNPAID);

        //设置orderInfo的总金额
        orderInfo.sumTotalAmount();

        //设置订单备注信息
        if (StringUtils.isEmpty(orderInfo.getOrderComment())){
            orderInfo.setOrderComment("==订单备注信息==");
        }

        //设置订单的处理状态
        orderInfo.setProcessStatus(ProcessStatus.UNPAID);

        //设置订单所属用户id
        String userId = (String) request.getAttribute("userId");
        orderInfo.setUserId(userId);

        //校验订单流水单号是否一致，防止表单重复提交
        boolean flag = orderService.verifyTradeNo(tradeNo, userId);

        if (!flag){
            request.setAttribute("errMsg", ORDER_SUBMIT_REPEAT);
            return "tradeFail";
        }

        //校验库存
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        if (orderDetailList != null && orderDetailList.size() > 0){
            for (OrderDetail orderDetail : orderDetailList) {

                boolean result = orderService.verifyStock(orderDetail.getSkuId(), orderDetail.getSkuNum());

                if (!result){
                    request.setAttribute("errMsg", orderDetail.getSkuName() + SKU_UNDER_STOCK);
                    return "tradeFail";
                }

                //校验商品价格是否已更新
                SkuInfo skuInfo = skuManageService.getSkuInfoBySkuId(orderDetail.getSkuId());

                if (skuInfo.getPrice().compareTo(orderDetail.getOrderPrice()) != 0){
                    //更新订单中商品价格
                    orderDetail.setOrderPrice(skuInfo.getPrice());
                }
            }
        }

        String orderId = orderService.saveOrder(orderInfo);

        return "redirect:http://payment.gmall.com/toPay?orderId="+orderId;
    }

    /**
     * 跳转到订单页面
     * @param request
     * @return
     */
    @RequestMapping("/trade")
    @LoginCheckAnnotation
    public String index(HttpServletRequest request) {

        String userId = (String) request.getAttribute("userId");

        //获取购物车中勾选商品的列表集合
        List<CartInfo> cartInfoListChecked = cartService.getCheckedCartInfoList(userId);

        List<UserAddress> userAddressList = getUserAddressByUserId(request);

        //获取结算商品的总金额
        OrderInfo orderInfo = new OrderInfo();
        List<OrderDetail> orderDetailList = new ArrayList<>();
        if (cartInfoListChecked != null && cartInfoListChecked.size() > 0){
            for (CartInfo cartInfo : cartInfoListChecked) {
                OrderDetail orderDetail = new OrderDetail();

                orderDetail.setSkuId(cartInfo.getSkuId());
                orderDetail.setSkuName(cartInfo.getSkuName());
                orderDetail.setSkuNum(cartInfo.getSkuNum());
                orderDetail.setOrderPrice(cartInfo.getCartPrice());
                orderDetail.setImgUrl(cartInfo.getImgUrl());

                orderDetailList.add(orderDetail);
            }
        }

        orderInfo.setOrderDetailList(orderDetailList);

        orderInfo.sumTotalAmount();
        BigDecimal totalAmount = orderInfo.getTotalAmount();

        //获取一个流水订单号tradeNo,用来防止表单重复提交
        String tradeNo = orderService.getTradeNo(userId);

        request.setAttribute("cartInfoListChecked", cartInfoListChecked);
        request.setAttribute("userAddressList", userAddressList);
        request.setAttribute("totalAmount", totalAmount);
        request.setAttribute("tradeNo", tradeNo);

        return "trade";
    }

    /**
     * 获取用户地址信息
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping("/userAddress")
    @LoginCheckAnnotation
    public List<UserAddress> getUserAddressByUserId(HttpServletRequest request) {

        String userId = (String) request.getAttribute("userId");
        return userService.getUserAddressById(userId);
    }

}
