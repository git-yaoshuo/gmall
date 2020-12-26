package com.yaoshuo.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.yaoshuo.gmall.annotation.LoginCheckAnnotation;
import com.yaoshuo.gmall.bean.cart.CartInfo;
import com.yaoshuo.gmall.bean.manage.SkuInfo;
import com.yaoshuo.gmall.cart.handler.CartCookieHandler;
import com.yaoshuo.gmall.service.cart.CartService;
import com.yaoshuo.gmall.service.manage.SkuManageService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
public class CartInfoController {

    @Reference
    private CartService cartService;

    @Reference
    private SkuManageService skuManageService;

    @Autowired
    private CartCookieHandler cartCookieHandler;

    /**
     * 去结算,跳转到订单页面
     *  http://cart.gmall.com/toTrade
     * @return
     */
    @RequestMapping("/toTrade")
    @LoginCheckAnnotation
    public String toTrade(HttpServletRequest request, HttpServletResponse response){

        String userId = (String) request.getAttribute("userId");

        List<CartInfo> cookieCartSkuList = cartCookieHandler.getCookieCartSkuList(request);

        if (cookieCartSkuList != null && cookieCartSkuList.size() > 0){

            cartService.mergeCartInfoList(cookieCartSkuList, userId);
            cartCookieHandler.deleteCookidCart(request,response);
        }

        return "redirect:http://order.gmall.com/trade";
    }

    @ResponseBody
    @RequestMapping("/checkCart")
    @LoginCheckAnnotation(isVerify = false)
    public void checkCartInfo(String isChecked, String skuId, HttpServletRequest request, HttpServletResponse response){
        String userId = (String) request.getAttribute("userId");

        if(StringUtils.isEmpty(userId)){
            //用户未登录
            cartCookieHandler.checkCart(isChecked,skuId,request,response);
        }else {
            //用户已登录
            cartService.checkCart(isChecked,skuId,userId);
        }
    }

    /**
     * 根据用户id查询用户添加的所有商品列表，并去结算
     * http://cart.gmall.com/cartList
     */
    @RequestMapping("/cartList")
    @LoginCheckAnnotation(isVerify = false)
    public String toCartList(HttpServletRequest request, HttpServletResponse response){

        String userId = (String) request.getAttribute("userId");

        List<CartInfo> cartInfoList = null;

        if (StringUtils.isEmpty(userId)){
            //用户未登录
            cartInfoList = cartCookieHandler.getCookieCartSkuList(request);
        }else {
            //用户已登录

            /*合并购物车

                1、合并用户未登录之前,往购物车中添加的商品信息(根据cookie来判断)，同步到一个列表中展示
                2、如果数据库中存在与之对应的skuId商品，数量更新
                3、如果数据库中没有与之对应的skuId商品，则添加到数据库中，更新缓存
             */
            List<CartInfo> cookieCartSkuList = cartCookieHandler.getCookieCartSkuList(request);

            if (cookieCartSkuList != null && cookieCartSkuList.size() > 0){
                //合并购物车
                cartInfoList = cartService.mergeCartInfoList(cookieCartSkuList,userId);

                //删除cookie中的购物车信息
                cartCookieHandler.deleteCookidCart(request, response);
            }else {
                cartInfoList = cartService.getCartSkuListByUserId(userId);
            }
        }

        request.setAttribute("cartInfoList", cartInfoList);

        return "cartList";
    }

    /**
     * 添加商品到购物车
     * http://cart.gmall.com/addToCart
     * @return
     */
    @RequestMapping("/addToCart")
    @LoginCheckAnnotation(isVerify = false)
    public String addToCart(Integer skuNum, String skuId, HttpServletRequest request, HttpServletResponse response){

        String userId = (String) request.getAttribute("userId");

        if (!StringUtils.isEmpty(userId)){
            //用户已登录
            cartService.addToCart(skuId, skuNum, userId);
        }else{
            //用户未登录
            cartCookieHandler.addToCartCookie(request,response,skuNum,skuId,userId);
        }

        //根据商品skuId查询商品信息，保存到request作用域中
        SkuInfo skuInfo = skuManageService.getSkuInfoBySkuId(skuId);

        request.setAttribute("skuInfo",skuInfo);
        request.setAttribute("skuNum",skuNum);

        return "success";
    }

    /**
     * 根据skuId删除购物车中的商品
     * @param skuId
     * @return
     */
    @RequestMapping("/delCartSku")
    @LoginCheckAnnotation(isVerify = false)
    public String delCartSkuBySkuId(String skuId,HttpServletRequest request, HttpServletResponse response){
        System.err.println(skuId);

        String userId = (String) request.getAttribute("userId");
        if (StringUtils.isEmpty(userId)){
            //未登录
            cartCookieHandler.delCartSkuBySkuId(skuId, request, response);
        }else {
            //已登录
            cartService.delCartSkuBySkuId(skuId,userId);
        }
        return "redirect:/cartList";
    }

    /**
     * 修改购物车中商品的数量
     * @param skuNum
     */
    @RequestMapping("/updateCartSkuNum")
    @LoginCheckAnnotation(isVerify = false)
    public String updateCartSkuNum(String skuId, Integer skuNum, HttpServletRequest request, HttpServletResponse response){

        String userId = (String) request.getAttribute("userId");
        if (StringUtils.isEmpty(userId)){
            //未登录
            cartCookieHandler.updateCartSkuNumBySkuId(skuId, skuNum, request, response);
        }else {
            //已登录
            cartService.updateCartSkuNumBySkuId(skuId, skuNum,userId);
        }
        return "redirect:/cartList";
    }

}
