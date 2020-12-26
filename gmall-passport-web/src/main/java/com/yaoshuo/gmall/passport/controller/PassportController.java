package com.yaoshuo.gmall.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.yaoshuo.gmall.bean.user.UserInfo;
import com.yaoshuo.gmall.util.JwtUtil;
import com.yaoshuo.gmall.service.user.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PassportController {

    @Reference
    private UserService userService;

    @Value("${token.key}")
    private String tokenKey;

    @ResponseBody
    @RequestMapping("/verify")
    public String verify(HttpServletRequest request){

        String token = request.getParameter("token");

        if (StringUtils.isEmpty(token)) {
            return "fail";
        }

        String salt = request.getHeader("X-forwarded-for");

        Map<String, Object> map = JwtUtil.decode(token, tokenKey, salt);

        if (map != null && map.size() > 0){
            String userId = (String) map.get("userId");
            UserInfo loginUser = userService.verify(userId);

            if (loginUser != null ){
                return "success";
            }
        }

        return "fail";
    }

    /**
     * 用户登录
     * @param userInfo
     * @return
     */
    @RequestMapping("/login")
    @ResponseBody
    public String login(UserInfo userInfo, HttpServletRequest request){

        UserInfo loginUser = userService.login(userInfo);

        if (loginUser != null){

            Map<String, Object> param = new HashMap<String, Object>();

            param.put("userId",loginUser.getId());
            param.put("nickName",loginUser.getNickName());

            String salt = request.getHeader("X-forwarded-for");
            System.err.println("salt:" + salt);

            return JwtUtil.encode(tokenKey, param, salt);
        }

        return "fail";
    }

    /**
     * 跳转到登录页面
     * @param originUrl
     * @param request
     * @return
     */
    @RequestMapping("/toLogin")
    public String toLogin(String originUrl, HttpServletRequest request){
        System.err.println("originUrl = " + originUrl);
        if (StringUtils.isEmpty(originUrl)) {
            String referer = request.getHeader("Referer");
            System.err.println(referer);
        }
        request.setAttribute("originUrl",originUrl);
        return "index";
    }
}
