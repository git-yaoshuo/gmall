package com.yaoshuo.gmall.service.cart;

import com.yaoshuo.gmall.bean.cart.CartInfo;

import java.util.List;

public interface CartService {


    /**
     * 用户登录后将商品添加到购物车
     * @param skuId
     * @param skuNum
     * @param userId
     */
    void addToCart(String skuId, Integer skuNum, String userId);

    /**
     * 根据用户id查询用户添加的所有商品列表
     * @param userId
     * @return
     */
    List<CartInfo> getCartSkuListByUserId(String userId);

    /**
     * 合并cookie中的购物车数据
     * @param cookieCartSkuList
     * @param userId
     * @return
     */
    List<CartInfo> mergeCartInfoList(List<CartInfo> cookieCartSkuList, String userId);

    /**
     * 登录用户更新购物车中商品勾选状态
     * @param isChecked
     * @param skuId
     * @param userId
     */
    void checkCart(String isChecked, String skuId, String userId);

    /**
     *  根据商品id删除购物车中的商品
     * @param skuId
     * @param userId
     */
    void delCartSkuBySkuId(String skuId, String userId);

    /**
     * 获取用户购物车中勾选的商品列表
     * @param userId
     * @return
     */
    List<CartInfo> getCheckedCartInfoList(String userId);

    /**
     * 修改购物车中商品的数量
     * @param skuId
     * @param skuNum
     * @param userId
     */
    void updateCartSkuNumBySkuId(String skuId, Integer skuNum, String userId);
}
