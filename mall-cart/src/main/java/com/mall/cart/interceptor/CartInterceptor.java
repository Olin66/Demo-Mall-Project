package com.mall.cart.interceptor;

import com.mall.common.constant.AuthConstant;
import com.mall.common.constant.CartConstant;
import com.mall.common.to.UserInfoTo;
import com.mall.common.vo.MemberRespVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;

@Component
public class CartInterceptor implements HandlerInterceptor {
    public static ThreadLocal<UserInfoTo> threadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        MemberRespVo attribute = (MemberRespVo) session.getAttribute(AuthConstant.LOGIN_USER);
        UserInfoTo user = new UserInfoTo();
        if (attribute != null) {
            user.setUserId(attribute.getId());
        }
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                String name = cookie.getName();
                if (name.equals(CartConstant.TEMP_USER_COOKIE_NAME)) {
                    user.setUserKey(cookie.getValue());
                    user.setTempUser(true);
                }
            }
        }
        if (StringUtils.isEmpty(user.getUserKey())) {
            String uuid = UUID.randomUUID().toString();
            user.setUserKey(uuid);
        }
        threadLocal.set(user);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        UserInfoTo user = threadLocal.get();
        if (user.getTempUser()) return;
        Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_NAME, user.getUserKey());
        cookie.setDomain("olinmall.com");
        cookie.setMaxAge(CartConstant.TEMP_USER_COOKIE_TIMEOUT);
        response.addCookie(cookie);
        user.setTempUser(true);
    }
}
