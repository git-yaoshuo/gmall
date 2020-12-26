package com.yaoshuo.gmall.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.yaoshuo.gmall.bean.user.UserAddress;
import com.yaoshuo.gmall.bean.user.UserInfo;
import com.yaoshuo.gmall.user.mapper.UserAddressMapper;
import com.yaoshuo.gmall.user.mapper.UserInfoMapper;
import com.yaoshuo.gmall.service.user.UserService;
import com.yaoshuo.gmall.util.JedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;

import java.util.List;

import static com.yaoshuo.gmall.constant.RedisConstant.*;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private UserAddressMapper userAddressMapper;

    /**
     * 获取所有用户信息
     * @return
     */
    @Override
    public List<UserInfo> findAll() {
        return userInfoMapper.selectAll();
    }

    /**
     * 根据用户id获取用户地址信息
     * @param userId
     * @return
     */
    @Override
    public List<UserAddress> getUserAddressById(String userId) {

        UserAddress userAddress = new UserAddress();
        userAddress.setUserId(userId);

        return userAddressMapper.select(userAddress);
    }

    /**
     * 根据用户名和密码获取用户信息=====用户登录
     * @param userInfo
     * @return
     */
    @Override
    public UserInfo login(UserInfo userInfo) {

        UserInfo loginUser = null;
        Jedis jedis = null;

        try {
            String passwd = userInfo.getPasswd();

            //对密码进行md5加密
            String digestPasswd = DigestUtils.md5DigestAsHex(passwd.getBytes());
            userInfo.setPasswd(digestPasswd);

            loginUser = userInfoMapper.selectOne(userInfo);

            if (loginUser != null){

                jedis = JedisUtils.getJedis();

                String userInfoKey = REDIS_PREFIX_USER + loginUser.getId() + REDIS_SUFFIX_USER_INFO;

                loginUser.setPasswd("");
                jedis.setex(userInfoKey, REDIS_USER_LOGIN_TIME_OUT, JSON.toJSONString(loginUser));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null){
                jedis.close();
            }
        }
        return loginUser;
    }

    /**
     * 根据用户id在redis缓存中查询是否登录
     * @param userId
     * @return
     */
    @Override
    public UserInfo verify(String userId) {

        Jedis jedis = JedisUtils.getJedis();

        String userInfoKey = REDIS_PREFIX_USER + userId + REDIS_SUFFIX_USER_INFO;

        String userInfoJsonStr = null;
        if (jedis.exists(userInfoKey)){
            userInfoJsonStr = jedis.get(userInfoKey);
        }

        if (!StringUtils.isEmpty(userInfoJsonStr)){

            jedis.setex(userInfoKey, REDIS_USER_LOGIN_TIME_OUT, userInfoJsonStr);

            return JSON.parseObject(userInfoJsonStr, UserInfo.class);
        }

        return null;
    }
}
