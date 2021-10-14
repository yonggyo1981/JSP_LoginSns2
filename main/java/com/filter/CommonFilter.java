package com.filter;

import javax.servlet.*;
import java.io.IOException;

import com.snslogin.*;

/**
 * 사이트 공통 필터
 *
 */
public class CommonFilter implements Filter {

	public void init(FilterConfig filterConfig) throws ServletException {
		try {
			NaverLogin.init(filterConfig);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
		chain.doFilter(request, response);
	}
}



