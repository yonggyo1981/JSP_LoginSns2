package com.controller;

import javax.servlet.*; 
import javax.servlet.http.*;
import java.io.IOException;

import com.snslogin.*;

public class MainController extends HttpServlet {
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html; charset=utf-8");
		SocialLogin naver = new NaverLogin();
		
		String naverCodeURL = naver.getCodeURL(request);
		request.setAttribute("naverCodeURL", naverCodeURL);
		
		RequestDispatcher rd = request.getRequestDispatcher("/main.jsp");
		rd.forward(request, response);
	}
}
