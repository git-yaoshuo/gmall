package com.yaoshuo.gmall.interceptor;

import com.alibaba.fastjson.JSON;
import com.yaoshuo.gmall.constant.WebConst;
import com.yaoshuo.gmall.util.CookieUtil;
import com.yaoshuo.gmall.annotation.LoginCheckAnnotation;
import io.jsonwebtoken.impl.Base64UrlCodec;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import com.yaoshuo.gmall.common.util.HttpClientUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String token = request.getParameter("newToken");

        if (!StringUtils.isEmpty(token)){
            CookieUtil.setCookie(request,response,"token",token, WebConst.COOKIE_MAXAGE,false);
        }

        if (StringUtils.isEmpty(token)) {
            token = CookieUtil.getCookieValue(request, "token", false);
        }

//        if (!StringUtils.isEmpty(token)){
//            Map map = getUserMapByToken(token);
//            String nickName = (String) map.get("nickName");
//            request.setAttribute("nickName", nickName);
//        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;

        LoginCheckAnnotation methodAnnotation = handlerMethod.getMethodAnnotation(LoginCheckAnnotation.class);
        if (methodAnnotation != null){

            String result = HttpClientUtil.doGet(WebConst.VERIFY_ADDRESS + "?token=" + token);

            if ("success".equals(result)){
                Map map = getUserMapByToken(token);
                String nickName = (String) map.get("nickName");
                String userId = (String) map.get("userId");

                request.setAttribute("nickName", nickName);
                request.setAttribute("userId",userId);

                return true;
            }else{
                if (methodAnnotation.isVerify()){
                    String originUrl = request.getRequestURL().toString();

                    String encodeOriginUrl = URLEncoder.encode(originUrl, "utf-8");

                    response.sendRedirect(WebConst.LOGIN_ADDRESS + "?originUrl=" + encodeOriginUrl);
                    return false;
                }
            }
        }

        return true;
    }

    private Map getUserMapByToken(String  token){

        String tokenUserInfo = StringUtils.substringBetween(token, ".");

        Base64UrlCodec base64UrlCodec = new Base64UrlCodec();
        byte[] tokenBytes = base64UrlCodec.decode(tokenUserInfo);

        String tokenJson = new String(tokenBytes, StandardCharsets.UTF_8);
        return JSON.parseObject(tokenJson, Map.class);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
