package com.yaoshuo.gmall.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.yaoshuo.gmall.bean.user.UserInfo;
import com.yaoshuo.gmall.service.user.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserController {

    @Reference
    private UserService userService;

    /**
     * 获取所有用户信息列表
     * @return
     */
    @GetMapping("/findAll")
    public List<UserInfo> findAll() {
        return userService.findAll();
    }
}
