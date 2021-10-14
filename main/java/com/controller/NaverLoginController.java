package com.controller;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;

import com.snslogin.*;
import java.util.*;

public class NaverLoginController extends HttpServlet {
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html; charset=utf-8");
		PrintWriter out = response.getWriter();
		try {
			SocialLogin naver = new NaverLogin();
			String accessToken = naver.getAccessToken(request);
			HashMap<String, String> userInfo = naver.getUserProfile(accessToken);
			if (userInfo == null) { // 네이버에서 프로필 정보 조회 실패
				throw new Exception("네이버 로그인 실패!");
			}
			
			// 네이버 아이디로 회원 가입이 되어 있는지 여부
			if (naver.isJoin(request, userInfo)) { // 이미 회원 가입이 되어 있는 경우 
				// 로그인 처리 
				boolean result = naver.login(request);
				if (!result) {
					throw new Exception("네이버 로그인 실패!");
				}
				
				out.print("<script>location.replace('main');</script>");
			} else {
				// 회원 가입으로 이동 
				out.print("<script>location.replace('member/join');</script>");
			}
			
			
			
		} catch (Exception e) {
			out.printf("<script>alert('%s');location.replace('member/login');</script>", e.getMessage());
		}
	}
}
