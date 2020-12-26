package com.yaoshuo.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.yaoshuo.gmall.bean.cart.CartInfo;
import com.yaoshuo.gmall.bean.manage.SkuInfo;
import com.yaoshuo.gmall.cart.mapper.CartInfoMapper;
import com.yaoshuo.gmall.service.cart.CartService;
import com.yaoshuo.gmall.service.manage.SkuManageService;
import com.yaoshuo.gmall.util.JedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.yaoshuo.gmall.constant.RedisConstant.*;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartInfoMapper cartInfoMapper;

    @Reference
    private SkuManageService skuManageService;

    /**
     * 用户登陆后将商品放到购物车中
     * @param skuId
     * @param skuNum
     * @param userId
     */
    @Override
    @Transactional
    public void addToCart(String skuId, Integer skuNum, String userId) {
        /*
            1、先根据用户id和商品id查询购物车中是否存在该商品
            2、若存在，将数量更新
            3、如不存在，则根据skuId查询商品信息，然后直接添加商品到购物车中
         */

        Jedis jedis = null;

        try {
            CartInfo cartInfo = new CartInfo();
            cartInfo.setSkuId(skuId);
            cartInfo.setUserId(userId);
            CartInfo cartInfoExists = cartInfoMapper.selectOne(cartInfo);

            if (cartInfoExists != null){

                cartInfoExists.setSkuNum(cartInfoExists.getSkuNum() + skuNum);
                cartInfoMapper.updateByPrimaryKeySelective(cartInfoExists);

                //给商品一个初始化的实时价格
                cartInfo.setSkuPrice(cartInfoExists.getCartPrice());

            }else {
                SkuInfo skuInfo = skuManageService.getSkuInfoBySkuId(skuId);

                if (skuInfo != null){
                    CartInfo cartInfoSave = new CartInfo();

                    cartInfoSave.setSkuNum(skuNum);
                    cartInfoSave.setUserId(userId);
                    cartInfoSave.setSkuId(skuInfo.getId());
                    cartInfoSave.setCartPrice(skuInfo.getPrice());
                    cartInfoSave.setSkuPrice(skuInfo.getPrice());
                    cartInfoSave.setImgUrl(skuInfo.getSkuDefaultImg());
                    cartInfoSave.setSkuName(skuInfo.getSkuName());

                    cartInfoMapper.insertSelective(cartInfoSave);

                    cartInfoExists = cartInfoSave;
                }
            }

            //添加到缓存中
            jedis = JedisUtils.getJedis();
            String cartKey = REDIS_PREFIX_CART + userId + REDIS_SUFFIX_CART_SKU_INFO;
            jedis.hset(cartKey,skuId, JSON.toJSONString(cartInfoExists));

            //设置购物车过期时间====与用户登录时间一致
            setCartExpireTime(userId, jedis, cartKey);

        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (jedis != null){
                jedis.close();
            }
        }
    }

    /**
     * 已登录用户获取用户添加到购物车中的商品列表
     * @param userId
     * @return
     */
    @Override
    @Transactional
    public List<CartInfo> getCartSkuListByUserId(String userId) {

        /*
            1、先从缓存中获取用户添加的商品列表
            2、如果缓存中存在，则取出来
            3、如果缓存中不存在，则在数据库中查询

         */
        Jedis jedis = null;


        try {
            jedis = JedisUtils.getJedis();

            String cartKey = REDIS_PREFIX_CART + userId + REDIS_SUFFIX_CART_SKU_INFO;
            List<String> cartInfoJsonList = jedis.hvals(cartKey);

            List<CartInfo> cartInfoList = new ArrayList<>();

            if (cartInfoJsonList != null && cartInfoJsonList.size() > 0){
                for (String cartInfoStr : cartInfoJsonList) {

                    CartInfo cartInfo = JSON.parseObject(cartInfoStr, CartInfo.class);
                    cartInfoList.add(cartInfo);
                }

                //给cartInfoList排序======实际应该按照更新时间来进行排序
                cartInfoList.sort(new Comparator<CartInfo>() {
                    @Override
                    public int compare(CartInfo o1, CartInfo o2) {
                        return o1.getId().compareTo(o2.getId());
                    }
                });

            }else {
                cartInfoList = cartInfoMapper.selectCartInfoWithCurrentPriceByUserId(userId);

                //更新缓存
                if (cartInfoList != null && cartInfoList.size() > 0){
                    for (CartInfo cart : cartInfoList) {

                        cartInfoMapper.updateByPrimaryKeySelective(cart);

                        jedis.hset(cartKey,cart.getSkuId(),JSON.toJSONString(cart));
                    }
                }
            }

            //获取redis购物车中勾选的商品的列表，更新商品的勾选状态
            List<CartInfo> checkedCartInfoList = getCheckedCartInfoList(userId);
            if (checkedCartInfoList != null && checkedCartInfoList.size() > 0){
                for (CartInfo checkedCartInfo : checkedCartInfoList) {

                    if (cartInfoList !=null && cartInfoList.size() > 0){
                        for (CartInfo cartInfo : cartInfoList) {
                            if (cartInfo.getSkuId().equals(checkedCartInfo.getSkuId())){
                                cartInfo.setIsChecked(checkedCartInfo.getIsChecked());
                                jedis.hset(cartKey,cartInfo.getSkuId(),JSON.toJSONString(cartInfo));
                            }
                        }
                    }
                }

                setCartExpireTime(userId, jedis, cartKey);
            }


            return cartInfoList;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null){
                jedis.close();
            }
        }
        return null;
    }

    /**
     * 合并cookie中购物车中的数据
     * @param cookieCartSkuList
     * @param userId
     * @return
     */
    @Override
    @Transactional
    public List<CartInfo> mergeCartInfoList(List<CartInfo> cookieCartSkuList, String userId) {

        /*合并购物车

            1、合并用户未登录之前,往购物车中添加的商品信息(根据cookie来判断)，同步到一个列表中展示
            2、如果数据库中存在与之对应的skuId商品，数量更新
            3、如果数据库中没有与之对应的skuId商品，则添加到数据库中，更新缓存
         */
        List<CartInfo> cartInfoList = getCartSkuListByUserId(userId);

        boolean isMatch = false;

        for (CartInfo cookieCartSku : cookieCartSkuList) {
            //36,32
            if (cartInfoList != null && cartInfoList.size() > 0){
                for (CartInfo cartInfo : cartInfoList) {
                    //36,38
                    if (cartInfo.getSkuId().equals(cookieCartSku.getSkuId())){

                        //重新设置商品的购买次数
                        cartInfo.setSkuNum(cartInfo.getSkuNum() + cookieCartSku.getSkuNum());

                        cartInfoMapper.updateByPrimaryKeySelective(cartInfo);

                        isMatch = true;
                        break;
                    }
                }
            }

            //未匹配上的添加到数据库中
            if (!isMatch){
                //设置用户id
                cookieCartSku.setUserId(userId);
                cartInfoMapper.insertSelective(cookieCartSku);
            }
        }

        //重新查询数据库中的购物车商品列表
        List<CartInfo> cartInfoListUpdated = cartInfoMapper.selectCartInfoWithCurrentPriceByUserId(userId);

        //更新缓存
        Jedis jedis = null;

        try {
            jedis = JedisUtils.getJedis();
            String cartKey = REDIS_PREFIX_CART + userId + REDIS_SUFFIX_CART_SKU_INFO;

            if (cartInfoListUpdated != null && cartInfoListUpdated.size() > 0){
                for (CartInfo cart : cartInfoListUpdated) {
                    jedis.hset(cartKey,cart.getSkuId(),JSON.toJSONString(cart));
                }

                setCartExpireTime(userId, jedis, cartKey);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (jedis != null){
                jedis.close();
            }
        }

        //用户未登录点击结算时，需要将cookie中的商品合并到登录状态下。需要重新设置勾选状态=====以cookie为准
        if (cartInfoListUpdated != null && cartInfoListUpdated.size() > 0){

            for (CartInfo cartInfo : cartInfoListUpdated) {

                for (CartInfo cookieCartInfo : cookieCartSkuList) {
                    if (cartInfo.getSkuId().equals(cookieCartInfo.getSkuId())){

                        //重置redis中商品的勾选状态，与cookie中商品勾选状态保持一致
                        if ("1".equals(cookieCartInfo.getIsChecked())){

                            checkCart(cookieCartInfo.getIsChecked(),cartInfo.getSkuId(),userId);
                        }
                        break;
                    }
                }
            }
        }

        return cartInfoListUpdated;
    }

    /**
     * 登录用户更新购物车中商品勾选状态
     * @param isChecked
     * @param skuId
     * @param userId
     */
    @Override
    public void checkCart(String isChecked, String skuId, String userId) {

        /*
            1、先从缓存中获取购物车商品与之skuId对应的商品
            2、反序列化为对象，设置isChecked的值，然后在序列化为json串
            3、保存回redis缓存中
            4、将选中的商品放到另一个reids缓存中，方便结算
         */
        Jedis jedis = null;

        try {
            jedis = JedisUtils.getJedis();
            String cartKey = REDIS_PREFIX_CART + userId + REDIS_SUFFIX_CART_SKU_INFO;
            String cartInfoJson = jedis.hget(cartKey, skuId);

            CartInfo cartInfo = JSON.parseObject(cartInfoJson, CartInfo.class);
            cartInfo.setIsChecked(isChecked);

            jedis.hset(cartKey,skuId,JSON.toJSONString(cartInfo));

            setCartExpireTime(userId,jedis,cartKey);


            //将选中的商品放到另一个reids缓存中，方便结算
            String checkedCartKey = REDIS_PREFIX_CART + userId + REDIS_SUFFIX_CHECKDE_CART_SKU_INFO;
            if ("1".equals(isChecked)){
                jedis.hset(checkedCartKey,skuId,JSON.toJSONString(cartInfo));

                setCartExpireTime(userId,jedis,checkedCartKey);
            }else {
                jedis.hdel(checkedCartKey,skuId);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null){
                jedis.close();
            }
        }

    }

    /**
     * 根据商品skuId删除购物车中的商品
     * @param skuId
     * @param userId
     */
    @Override
    public void delCartSkuBySkuId(String skuId, String userId) {
        Jedis jedis = null;

        try {
            jedis = JedisUtils.getJedis();
            String cartKey = REDIS_PREFIX_CART + userId + REDIS_SUFFIX_CART_SKU_INFO;

            List<String> cartInfoJsonList = jedis.hvals(cartKey);


            if (cartInfoJsonList != null && cartInfoJsonList.size() > 0) {
                for (String cartInfoStr : cartInfoJsonList) {

                    CartInfo cartInfo = JSON.parseObject(cartInfoStr, CartInfo.class);
                    if (cartInfo.getSkuId().equals(skuId)) {
                        jedis.hdel(cartKey, skuId);
                        break;
                    }
                }
            }

            //删除数据库中与之对应skuId的购物车商品信息
            CartInfo cartInfoDeleted = new CartInfo();
            cartInfoDeleted.setUserId(userId);
            cartInfoDeleted.setSkuId(skuId);

            cartInfoMapper.delete(cartInfoDeleted);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null){
                jedis.close();
            }
        }

    }

    /**
     * 获取用户购物车中勾选的商品列表
     * @param userId
     * @return
     */
    @Override
    public List<CartInfo> getCheckedCartInfoList(String userId) {

        List<CartInfo> checkedCartInfoList = null;
        Jedis jedis = null;
        try {
            checkedCartInfoList = new ArrayList<CartInfo>();
            jedis = JedisUtils.getJedis();

            String checkedCartKey = REDIS_PREFIX_CART + userId + REDIS_SUFFIX_CHECKDE_CART_SKU_INFO;

            List<String> checkedCartJsonList = jedis.hvals(checkedCartKey);

            if (checkedCartJsonList != null && checkedCartJsonList.size() > 0){

                for (String checkedCartJson : checkedCartJsonList) {
                    checkedCartInfoList.add(JSON.parseObject(checkedCartJson,CartInfo.class));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (jedis != null){
                jedis.close();
            }
        }

        return checkedCartInfoList;
    }

    /**
     * 已登录用户修改购物车中商品的数量
     * @param skuId
     * @param skuNum
     * @param userId
     */
    @Transactional
    @Override
    public void updateCartSkuNumBySkuId(String skuId, Integer skuNum, String userId) {

        Jedis jedis = null;
        try {
            jedis = JedisUtils.getJedis();

            String cartKey = REDIS_PREFIX_CART + userId + REDIS_SUFFIX_CART_SKU_INFO;
            String checkedCartKey = REDIS_PREFIX_CART + userId + REDIS_SUFFIX_CHECKDE_CART_SKU_INFO;

            List<CartInfo> cartSkuListByUserId = getCartSkuListByUserId(userId);
            if (cartSkuListByUserId != null && cartSkuListByUserId.size() > 0){
                for (CartInfo cartInfo : cartSkuListByUserId) {

                    if (cartInfo.getSkuId().equals(skuId)) {
                        cartInfo.setSkuNum(skuNum);
                        jedis.hset(cartKey,cartInfo.getSkuId(),JSON.toJSONString(cartInfo));
                        setCartExpireTime(userId,jedis,cartKey);

                        cartInfoMapper.updateByPrimaryKey(cartInfo);
                    }
                }
            }

            List<CartInfo> checkedCartInfoList = getCheckedCartInfoList(userId);

            if (checkedCartInfoList != null && checkedCartInfoList.size() > 0){

                for (CartInfo cartInfo : checkedCartInfoList) {

                    if (cartInfo.getSkuId().equals(skuId)) {
                        cartInfo.setSkuNum(skuNum);
                        jedis.hset(checkedCartKey,cartInfo.getSkuId(),JSON.toJSONString(cartInfo));
                        setCartExpireTime(userId,jedis,checkedCartKey);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (jedis != null){
                jedis.close();
            }
        }

    }

    /**
     * 设置redis缓存中购物车key的过期时间=====与用户登录过期时间保持一致
     * @param userId
     * @param jedis
     * @param cartKey
     */
    private void setCartExpireTime(String userId, Jedis jedis, String cartKey) {
        String userInfoKey = REDIS_PREFIX_USER + userId + REDIS_SUFFIX_USER_INFO;
        Long cartExpireTime = jedis.ttl(userInfoKey);
        jedis.expire(cartKey, cartExpireTime.intValue());
    }
}
