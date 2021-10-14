<%@ page contentType="text/html; charset=utf-8" %>
<%
	String naverCodeURL = (String)request.getAttribute("naverCodeURL");
%>
<a href='<%=naverCodeURL%>'>네이버 아이디로 로그인</a>