package com.snslogin;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Iterator;
import java.net.*;
import java.io.*;

import org.json.simple.*;
import org.json.simple.parser.*;

import com.models.dto.*;

/**
 * �Ҽ� �α��� �߻� Ŭ���� 
 *
 */
public abstract class SocialLogin {
	/** code �߱� URL */
	public abstract String getCodeURL(HttpServletRequest request);
	
	/** access token�߱� */
	public abstract String getAccessToken(HttpServletRequest request, String code, String state) throws Exception;
	public abstract String getAccessToken(HttpServletRequest request) throws Exception;
	
	/** ȸ�� ������ ��ȸ */
	public abstract HashMap<String, String> getUserProfile(String accessToken);
	
	/**
	 * 소셜 채널로 회원 가입이 되어 있는지 여부
	 * @param userInfo
	 * @return true - 이미 가입 -> 로그인 처리, false - 가입이 X -> 회원 가입
	 */
	public abstract boolean isJoin(HttpServletRequest request, HashMap<String, String> userInfo);
	
	
	/**
	 * 소셜 채널 로그인 처리 - session에 userInfo를 가지고 로그인 처리 
	 * 
	 * @param request
	 * @return
	 */
	public abstract boolean login(HttpServletRequest request);
	
	
	/**
	 * 세션에 담겨 있는 userInfo ->  관리하기 편하도록 Member 자바빈 클래스에 담아서 반환
	 * @param request
	 * @return
	 */
	public abstract Member getSocialUserInfo(HttpServletRequest request);
	
	/**
	 * HTTP URL ��û ������ 
	 * 
	 * @param ApiURL
	 */
	public JSONObject httpRequest(String apiURL, HashMap<String, String> reqHeaders) throws Exception {
		URL url = new URL(apiURL);
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setRequestMethod("GET");
		
		if (reqHeaders != null) {
			Iterator<String> ir = reqHeaders.keySet().iterator();
			while(ir.hasNext()) {
				String key = ir.next();
				String value = reqHeaders.get(key);
				conn.setRequestProperty(key, value);
			}
		}
		
		int statusCode = conn.getResponseCode();
		InputStream in;
		if (statusCode == HttpURLConnection.HTTP_OK) {
			in = conn.getInputStream();
		} else {
			in = conn.getErrorStream();
		}
		
		StringBuilder sb = null;
		try (in;
			InputStreamReader isr = new InputStreamReader(in);
			BufferedReader br = new BufferedReader(isr)) {
			
			String line; 
			sb = new StringBuilder();
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
 		
	
		JSONObject json = (JSONObject)new JSONParser().parse(sb.toString());
		
		return json;
	}
	
	public JSONObject httpRequest(String apiURL) throws Exception {
		return httpRequest(apiURL, null);
	}
}

