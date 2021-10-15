package com.controller.member;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;

import com.models.dao.MemberDao;
import com.exception.AlertException;

import com.models.dto.*;
import com.snslogin.*;

/**
 * 회원 가입 
 * GET - 가입 양식, POST - 가입 처리 
 */
public class JoinController extends HttpServlet {
	
	/** 회원가입 양식 */
	@Override 
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html; charset=utf-8");
		
		SocialLogin naver = new NaverLogin();
		
		Member socialMember = naver.getSocialUserInfo(request);
		boolean isSocialJoin = false;
		if (socialMember != null) {
			isSocialJoin = true;
		}
		
		request.setAttribute("isSocialJoin", isSocialJoin);
		request.setAttribute("member", socialMember);
		
		
		RequestDispatcher rd = request.getRequestDispatcher("/member/form.jsp");
		rd.include(request, response);
	}
	
	/** 회원가입 처리 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=utf-8");
		PrintWriter out = response.getWriter();
		try {
			SocialLogin naver = new NaverLogin();
			Member socialMember = naver.getSocialUserInfo(request);
			MemberDao dao = new MemberDao();
			boolean result = dao.join(request);
			if (!result) { // 회원가입 실패 
				throw new AlertException("회원가입 실패!");
			}	
			if (socialMember == null) { // 일반회원 
				out.print("<script>parent.location.href='login';</script>");
			} else { // 소셜 회원
				out.print("<script>parent.location.href='../main';</script>");
			}
		} catch (AlertException e) {
			out.print("<script>alert('" + e.getMessage() + "');</script>");
			return;
		}
	}
}





