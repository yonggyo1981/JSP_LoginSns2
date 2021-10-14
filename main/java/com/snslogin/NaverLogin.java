package com.snslogin;

import java.util.HashMap;
import java.util.Iterator;
import java.net.URLEncoder;
import java.sql.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.FilterConfig;

import org.json.simple.*;

import com.core.DB;
import com.models.dto.*;


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
		
		HttpSession session = request.getSession();
		session.setAttribute("state", state);
		
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
	public String getAccessToken(HttpServletRequest request, String code, String state) throws Exception {
	
		HttpSession session = request.getSession();
		String _state = String.valueOf((Long)session.getAttribute("state"));
		if (!_state.equals(state)) {
			throw new Exception("�����Ͱ� ���� �Ǿ����ϴ�.");
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("https://nid.naver.com/oauth2.0/token?");
		sb.append("grant_type=authorization_code");
		sb.append("&client_id=");
		sb.append(clientId);
		sb.append("&client_secret=");
		sb.append(clientSecret);
		sb.append("&code=");
		sb.append(code);
		sb.append("&state=");
		sb.append(state);
		
		String apiURL = sb.toString();
		JSONObject json = httpRequest(apiURL); 
		
		String accessToken = null;
		if (json.containsKey("access_token")) {
			accessToken = (String)json.get("access_token");
		}
		
		return accessToken;
	}

	@Override
	public String getAccessToken(HttpServletRequest request) throws Exception {	
		String code = request.getParameter("code");
		String state = request.getParameter("state");
		
		return getAccessToken(request, code, state);
	}

	@Override
	public HashMap<String, String> getUserProfile(String accessToken) {
		String apiURL = "https://openapi.naver.com/v1/nid/me";
		HashMap<String, String> headers = new HashMap<>();
		headers.put("Authorization", "Bearer " + accessToken);
		
		HashMap<String, String> userInfo = null;
		try {
			JSONObject result = httpRequest(apiURL, headers);
			String resultCode = (String)result.get("resultcode");
			if (resultCode.equals("00")) { // ���� ���� �� 
				userInfo = new HashMap<String, String>();
				
				JSONObject response = (JSONObject)result.get("response");
				
				Iterator<String> ir = response.keySet().iterator();
				while(ir.hasNext()) {
					String key = ir.next();
					String value = (String)response.get(key);
					userInfo.put(key, value);
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return userInfo;
	}
	
	@Override
	public boolean isJoin(HttpServletRequest request, HashMap<String, String> userInfo) {
		
		if (userInfo == null)
			return false;
		
		/** 
		 * userInfo -> 회원가입 URL로 넘어가도 데이터를 유지 -> 세션을 이용
		 */
		HttpSession session = request.getSession();
		session.setAttribute("naverUserInfo", userInfo);
		
		String socialChannel = "Naver";
		String socialId = userInfo.get("id");
		
		
		String sql = "SELECT COUNT(*) cnt FROM member WHERE socialChannel = ? AND socialId = ?";
		try (Connection conn = DB.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1,socialChannel);
			pstmt.setString(2, socialId);
			
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				int cnt = rs.getInt("cnt");
				if (cnt > 0) {
					return true; //이미 가입된 경우 -> 로그인 처리 
				}
			}
			
			rs.close();
			
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		
		return false;
	}
	
	@Override
	public boolean login(HttpServletRequest request) {
		
		HttpSession session = request.getSession();
		HashMap<String, String> userInfo = (HashMap<String, String>) session.getAttribute("naverUserInfo");
		String id = userInfo.get("id");
		String channel = "Naver";
		
		String sql = "SELECT memNo FROM member WHERE socialChannel = ? AND socialId = ?";
		try (Connection conn = DB.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, channel);
			pstmt.setString(2, id);
			
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				int memNo = rs.getInt("memNo");
				session.setAttribute("memNo", memNo);
				return true;
			}
			rs.close();
			
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		
		return false;
	}
	
	@Override
	public Member getSocialUserInfo(HttpServletRequest request) {
		Member member = null;
		HttpSession session = request.getSession();
		HashMap<String, String> userInfo = (HashMap<String, String>)session.getAttribute("naverUserInfo");
		String memId = String.valueOf(System.currentTimeMillis());
		if (userInfo.containsKey("email")) {
			String email = userInfo.get("email");
			memId = email.substring(0, email.lastIndexOf("@"));
		}
		
		member = new Member(
			0,
			memId,
			null,
			userInfo.get("name"),
			"Naver",
			userInfo.get("id"),
			null
		);
		
		return member;
	}
}











