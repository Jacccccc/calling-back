package com.example.demo.config;

import com.example.demo.util.bucket;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;

/**
 * @description:
 * @author: Jac
 * @time: 2022/3/5 14:31
 **/
@WebFilter(filterName = "RateLimit", urlPatterns = {"/post/hot"})
public class RateLimitFilter implements Filter {
    static bucket b=new bucket();
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if(b.get()==-1) {servletRequest.getRequestDispatcher("/busy").forward(servletRequest,servletResponse);}
        else filterChain.doFilter(servletRequest,servletResponse);
    }
}
