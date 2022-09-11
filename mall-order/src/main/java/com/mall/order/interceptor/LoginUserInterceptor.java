package com.mall.order.interceptor;

import com.mall.common.constant.AuthConstant;
import com.mall.common.vo.MemberRespVo;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class LoginUserInterceptor implements HandlerInterceptor {

    public static ThreadLocal<MemberRespVo> loginUser = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        boolean match = new AntPathMatcher().match("/order/order/status/**", request.getRequestURI())
                || new AntPathMatcher().match("/payed/notify", request.getRequestURI());
        if (match) return true;
        if (loginUser.get() != null) return true;
        MemberRespVo attribute = (MemberRespVo) request.getSession().getAttribute(AuthConstant.LOGIN_USER);
        if (attribute != null) {
            loginUser.set(attribute);
            return true;
        } else {
            request.getSession().setAttribute("msg", "请先进行登录！");
            response.sendRedirect("http://auth.olinmall.com/login.html");
            return false;
        }
    }
}
