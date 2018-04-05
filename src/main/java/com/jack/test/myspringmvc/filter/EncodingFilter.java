package com.jack.test.myspringmvc.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by jie on 2018/4/5.
 * 请求与响应编码设置
 */
public class EncodingFilter implements Filter{

    private String encoding = "UTF-8";

    public void init(FilterConfig filterConfig) throws ServletException {
        String encodingParam = filterConfig.getInitParameter("encoding");
        if(encodingParam != null){
            encoding = encodingParam;
        }
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        servletRequest.setCharacterEncoding(encoding);
        filterChain.doFilter(servletRequest,servletResponse);
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
        servletResponse.setCharacterEncoding(encoding);
    }

    public void destroy() {

    }
}
