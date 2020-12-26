package com.yaoshuo.gmall.cart.handler;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.yaoshuo.gmall.bean.cart.CartInfo;
import com.yaoshuo.gmall.bean.manage.SkuInfo;
import com.yaoshuo.gmall.service.manage.SkuManageService;
import com.yaoshuo.gmall.util.CookieUtil;
import com.yaoshuo.gmall.constant.WebConst;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@Component
public class CartCookieHandler {

    private String cartCookieName = WebConst.CART_COOKIE_NAME;

    @Reference
    private SkuManageService skuManageService;

    /**
     * 用户未登录则将商品信息暂存到cookie中
     * @param request
     * @param response
     * @param skuNum
     * @param skuId
     * @param userId
     */
    public void addToCartCookie(HttpServletRequest request, HttpServletResponse response, Integer skuNum, String skuId, String userId) {

        /*
            1、先获取cookie中的cart的值
            2、如果存在，则将其序列化为cartInfo集合
            3、遍历集合，判断集合中是否存在与skuId对应的商品
            4、有的话，则将数量更新即可
            5、没有的话，则将其添加到添加到cartInfo集合中
            6、最后将cartInfo反序列化，添加到cookie中
         */

        List<CartInfo> cartInfoList = getCookieCartSkuList(request);

        List<CartInfo> cartInfos = new ArrayList<>();

        //做一个标记判断cookie值中是否有对应skuId的商品
        boolean flag = false;

        if (cartInfoList != null && cartInfoList.size() > 0){
            for (CartInfo cartInfo : cartInfoList) {

                if (cartInfo.getSkuId().equals(skuId)){
                    
                    cartInfo.setSkuNum(cartInfo.getSkuNum() + skuNum);
                    cartInfo.setSkuPrice(cartInfo.getCartPrice());

                    flag = true;
                }
                cartInfos.add(cartInfo);
            }
        }

        //购物车cookie不存在 或者cookie中不存在与该skuId对应的商品
        if (!flag){
            SkuInfo skuInfo = skuManageService.getSkuInfoBySkuId(skuId);

            if (skuInfo != null){
                CartInfo cartInfo = new CartInfo();

                cartInfo.setSkuNum(skuNum);
                cartInfo.setUserId(userId);
                cartInfo.setSkuId(skuInfo.getId());
                cartInfo.setCartPrice(skuInfo.getPrice());
                cartInfo.setSkuPrice(skuInfo.getPrice());
                cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
                cartInfo.setSkuName(skuInfo.getSkuName());

                cartInfos.add(cartInfo);
            }
        }

        String cartInfoListCookieValue = JSON.toJSONString(cartInfos);

        CookieUtil.setCookie(request, response, cartCookieName,cartInfoListCookieValue, WebConst.COOKIE_MAXAGE,true);

    }

    /**
     * 未登录用户获取cookie中的cart购物车商品信息列表
     * @param request
     * @return
     */
    public List<CartInfo> getCookieCartSkuList(HttpServletRequest request) {

        String cartCookieValue = CookieUtil.getCookieValue(request, cartCookieName, true);
        return JSON.parseArray(cartCookieValue, CartInfo.class);
    }

    /**
     * 删除cookie中的购物车信息
     * @param request
     * @param response
     */
    public void deleteCookidCart(HttpServletRequest request, HttpServletResponse response) {
        CookieUtil.deleteCookie(request, response, cartCookieName);
    }

    /**
     * 未登录用户更新cookie中购物车商品信息的勾选状态
     * @param isChecked
     * @param skuId
     * @param request
     */
    public void checkCart(String isChecked, String skuId, HttpServletRequest request, HttpServletResponse response) {
        /*
            1、先从cookie中获取用户添加的购物车商品，反序列化为cartInfo对象
            2、修改商品的isChecked的值，然后在序列化为json串
            3、保存到cookie中
         */

        List<CartInfo> cartInfoList = getCookieCartSkuList(request);

        if (cartInfoList != null && cartInfoList.size() > 0){
            for (CartInfo cartInfo : cartInfoList) {
                if(skuId.equals(cartInfo.getSkuId())){
                    cartInfo.setIsChecked(isChecked);
                }
            }
        }

        CookieUtil.setCookie(request,response,cartCookieName,JSON.toJSONString(cartInfoList),WebConst.COOKIE_MAXAGE,true);
    }

    /**
     * 未登录用户更具skuId删除购物车中商品
     * @param skuId
     * @param request
     * @param response
     */
    public void delCartSkuBySkuId(String skuId,HttpServletRequest request,HttpServletResponse response) {

        List<CartInfo> cartInfoList = getCookieCartSkuList(request);

        if (cartInfoList != null && cartInfoList.size() > 0){
            for (CartInfo cartInfo : cartInfoList) {
                if(skuId.equals(cartInfo.getSkuId())){
                   CookieUtil.deleteCookie(request,response,cartCookieName);
                }
            }
        }

        CookieUtil.setCookie(request,response,cartCookieName,JSON.toJSONString(cartInfoList),WebConst.COOKIE_MAXAGE,true);
    }

    /**
     * 未登录用户修改购物车中商品的数量
     * @param skuId
     * @param skuNum
     * @param request
     * @param response
     */
    public void updateCartSkuNumBySkuId(String skuId, Integer skuNum, HttpServletRequest request, HttpServletResponse response) {
        List<CartInfo> cookieCartSkuList = getCookieCartSkuList(request);

        if (cookieCartSkuList != null && cookieCartSkuList.size() > 0){
            for (CartInfo cartInfo : cookieCartSkuList) {

                if (cartInfo.getSkuId().equals(skuId)){
                    cartInfo.setSkuNum(skuNum);
                }
            }
            CookieUtil.setCookie(request,response,cartCookieName,JSON.toJSONString(cookieCartSkuList),WebConst.COOKIE_MAXAGE,true);
        }
    }
}
