package com.snslogin;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.FilterConfig;
import java.net.URLEncoder;

public class NaverLogin extends SocialLogin {
	
	private static String clientId;
	private static String clientSecret;
	private static String callbackURL;
	
	public static void init(FilterConfig config) throws Exception {
		init(
			config.getInitParameter("NaverClientId"),
			config.getInitParameter("NaverClientSecret"),
			config.getInitParameter("NaverCallbackURL")
		);
	}
	
	public static void init(String clientId, String clientSecret, String callbackURL) throws Exception {
		NaverLogin.clientId = clientId;
		NaverLogin.clientSecret = clientSecret;
		NaverLogin.callbackURL = URLEncoder.encode(callbackURL, "UTF-8");
	}
	
	@Override
	public String getCodeURL(HttpServletRequest request) {
		long state = System.currentTimeMillis();
		
		StringBuilder sb = new StringBuilder();
		sb.append("https://nid.naver.com/oauth2.0/authorize?");
		sb.append("response_type=code");
		sb.append("&client_id=");
		sb.append(clientId);
		sb.append("&redirect_uri=");
		sb.append(callbackURL);
		sb.append("&state=");
		sb.append(state);
		
		return sb.toString();
	}

	@Override
	public String getAccessToken(HttpServletRequest request, String code, String state) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAccessToken(HttpServletRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HashMap<String, String> getUserProfile(String accessToken) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
