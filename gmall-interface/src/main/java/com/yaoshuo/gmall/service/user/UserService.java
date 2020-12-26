package com.yaoshuo.gmall.service.user;

import com.yaoshuo.gmall.bean.user.UserAddress;
import com.yaoshuo.gmall.bean.user.UserInfo;

import java.util.List;

public interface UserService {

    /**
     * 获取所有用户信息
     * @return
     */
    List<UserInfo> findAll();

    /**
     * 根据用户id查询用户地址列表
     * @param userId
     * @return
     */
    List<UserAddress> getUserAddressById(String userId);

    /**
     * 根据用户名和密码进行登录
     * @param userInfo
     * @return
     */
    UserInfo login(UserInfo userInfo);

    /**
     * 根据用户id校验用户是否已经登录
     * @param userId
     * @return
     */
    UserInfo verify(String userId);
}
