package com.mall.order.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> {
            ServletRequestAttributes requestAttributes
                    = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (requestAttributes == null) return;
            HttpServletRequest request = requestAttributes.getRequest();
            String cookie = request.getHeader("Cookie");
            template.header("Cookie", cookie);
        };
    }
}
