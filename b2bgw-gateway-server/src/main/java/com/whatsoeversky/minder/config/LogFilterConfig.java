package com.whatsoeversky.minder.config;

import com.whatsoeversky.minder.utils.DigestUtils;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;

@Configuration
public class LogFilterConfig {

    @Bean
    public FilterRegistrationBean<LogFilter> logFilterFilterRegistrationBean() {
        FilterRegistrationBean<LogFilter> logFilterFilterRegistrationBean = new FilterRegistrationBean<>();
        logFilterFilterRegistrationBean.addUrlPatterns("/*");
        logFilterFilterRegistrationBean.setFilter(new LogFilter());
        return logFilterFilterRegistrationBean;
    }

    @Slf4j
    public static class LogFilter implements Filter {

        @Override
        public void doFilter(ServletRequest servletRequest,
                             ServletResponse servletResponse,
                             FilterChain filterChain) throws IOException, ServletException {
            HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
            String requestURI = httpServletRequest.getRequestURI();
            log.debug("request uri: {}", requestURI);
            httpServletRequest.getParameterMap().forEach((key, value) -> {
                log.debug("request param, name: {}, value: {}", key, String.join(",", value));
            });
            Enumeration<String> headerNames = httpServletRequest.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String name = headerNames.nextElement();
                log.debug("request header, name: {}, value: {}", name, httpServletRequest.getHeader(name));
            }
//            ServletInputStream inputStream = httpServletRequest.getInputStream();
//            try {
//                log.debug("request body sha256: {}", DigestUtils.sha256Hex(inputStream));
//            } catch (NoSuchAlgorithmException e) {
//                throw new RuntimeException(e);
//            }
            filterChain.doFilter(servletRequest, servletResponse);
            HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
            int status = httpServletResponse.getStatus();
            log.debug("response status: {}", status);
            for (String headerName : httpServletResponse.getHeaderNames()) {
                log.debug("response header, name: {}, value: {}", headerName, httpServletResponse.getHeader(headerName));
            }
        }
    }
}
