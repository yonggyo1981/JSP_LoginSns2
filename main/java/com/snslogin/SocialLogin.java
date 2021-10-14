package com.snslogin;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

/**
 * 소셜 로그인 추상 클래스 
 *
 */
public abstract class SocialLogin {
	/** code 발급 URL */
	public abstract String getCodeURL(HttpServletRequest request);
	
	/** access token발급 */
	public abstract String getAccessToken(HttpServletRequest request, String code, String state);
	public abstract String getAccessToken(HttpServletRequest request);
	
	/** 회원 프로필 조회 */
	public abstract HashMap<String, String> getUserProfile(String accessToken);
}
