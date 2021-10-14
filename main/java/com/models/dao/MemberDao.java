package com.models.dao;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletRequest;
import java.sql.*;

import com.core.*;
import com.exception.*;
import org.mindrot.jbcrypt.*;

import com.models.dto.Member;

/**
 * 회원 Model
 *
 */
public class MemberDao {
	
	/**
	 * 로그인을 한 경우 전역에 회원 정보 유지
	 * 
	 * @param request
	 */
	public static void init(ServletRequest request) {
		/**
		 * 1. HttpSession에서 memNo의 존재 여부 - 있으면 - 로그인 상태
		 * 		 
		 * 2. memNo가 있으면 -> 회원정보를 가져와서 
		 *     request.setAttribute ....
		 */
		
		Member member = null;
		boolean isLogin = false;
		if (request instanceof HttpServletRequest) {
			HttpServletRequest req = (HttpServletRequest)request;
			HttpSession session = req.getSession();
			
			int memNo = 0;
			if (session.getAttribute("memNo") != null) {
				memNo = (Integer)session.getAttribute("memNo");
			}
			
			if (memNo > 0) { // 로그인 상태
				MemberDao dao = new MemberDao();
				member = dao.get(memNo);
				if (member != null) {
					isLogin = true;
				}
			} // endif 
		} // endif 
		
		request.setAttribute("member", member);
		request.setAttribute("isLogin", isLogin);
	}
	
	/**
	 * 회원 가입 처리 
	 * 
	 * @param request
	 * @return
	 */
	public boolean join(HttpServletRequest request) throws AlertException {
		
		// 입력 데이터 검증
		checkJoinData(request);
		
		String sql = "INSERT INTO member (memId, memPw, memNm) VALUES(?,?,?)";
		try (Connection conn = DB.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			String memId = request.getParameter("memId");
			String memPw = request.getParameter("memPw");
			String memNm = request.getParameter("memNm");
			
			String hash = BCrypt.hashpw(memPw, BCrypt.gensalt(10));
			
			pstmt.setString(1, memId);
			pstmt.setString(2, hash);
			pstmt.setString(3, memNm);
			
			int result = pstmt.executeUpdate();
			if (result < 1) 
				return false;
			
			/*
			ResultSet rs = pstmt.getGeneratedKeys();
			if (rs.next()) { // 추가된 회원 번호
				int memNo = rs.getInt(1);
			}
			rs.close();
			*/
			return true;
			
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * 회원 가입 유효성 검사
	 * 
	 * @param request
	 * @throws AlertException
	 */
	public void checkJoinData(HttpServletRequest request) throws AlertException {
		/**
		 * 0. 필수 항목 체크(아이디, 비밀번호, 비밀번호 확인, 회원명) - O 
		 * 1. 아이디 자리수(6~20), 영문, 숫자만 허용
		 * 2. 비밀번호 자리수(8자리 이상), 복잡성(최소 영문1개 이상, 최소 숫자1개 이상, 최소 특수문자 1개 이상
		 * 3. 아이디 중복 여부 체크(O)
		 * 4. 비밀번호 확인시 일치 여부  
		 */
		/** 필수 항목 체크 S */
		String[] required = {
			"memId//아이디를 입력하세요",
			"memPw//비밀번호를 입력하세요",
			"memPwRe//비밀번호를 확인해 주세요",
			"memNm//회원명을 입력하세요"
		};
		
		for (String s : required) {
			String[] re = s.split("//");
			
			if (request.getParameter(re[0]) == null || request.getParameter(re[0]).trim().equals("")) { // 필수 항목 누락일때 
				throw new AlertException(re[1]);
			}
		}
		/** 필수 항목 체크 E */
		/** 아이디 자리수, 영문 숫자로만 구성 체크 S */
		String memId = request.getParameter("memId").trim();
		
		if (memId.length() < 6 || memId.length() > 20) {
			throw new AlertException("아이디는 영문자,숫자 6자리 이상 20자리 이하로 입력해 주세요.");
		}
		/** 아이디 자리수 체크 E */
		
		/** 비밀번호 자리수 체크 S */
		String memPw = request.getParameter("memPw").trim();
		if (memPw.length() < 8) {
			throw new AlertException("비밀번호는 8자리 이상 입력해 주세요.");
		}
		/** 비밀전호 자리수 체크 E */
		
		/** 아이디 중복 여부 체크 S */
		String sql = "SELECT COUNT(*) cnt FROM member WHERE memId = ?";
		try (Connection conn = DB.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, memId);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				int cnt = rs.getInt("cnt");
				if (cnt > 0) { // 이미 등록된 아이디인 경우 
					throw new AlertException("이미 가입된 아이디 입니다 - " + memId);
				}
			}
			rs.close();
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		/** 아이디 중복 여부 체크 E */
		
		/** 비밀번호 확인시 일치 여부 S */
		String memPwRe = request.getParameter("memPwRe");
		if (!memPw.equals(memPwRe)) {
			throw new AlertException("비밀번호 확인을 다시한번 해 주세요.");
		}
		/** 비밀번호 확인시 일치 여부 E */
	}
	
	/**
	 * 로그인 처리 
	 * 
	 * @param HttpServletRequest request - 세션 처리(HttpSession)
	 * @param memId 
	 * @param memPw 
	 * @throws AlertException
	 */
	public void login(HttpServletRequest request, String memId, String memPw) throws AlertException {
		/**
		 * 1. 필수 항목 체크(아이디, 비번) - O
		 * 2. 아이디로 회원정보를 조회 - 별도의 메서드 - O
		 * 3. 회원 정보가 있으면 -> 비밀번호 해시 일치 여부 체크  - O 
		 * 4. 모든것이 일치하면 로그인 처리(세션에 memNo 값을 저장 - 전역에 유지)
		 */
		 /** 필수 항목 체크 S */
		if (memId == null || memId.trim().equals("")) {
			throw new AlertException("아이디를 입력해 주세요.");
		}
		
		if (memPw == null || memPw.trim().equals("")) {
			throw new AlertException("비밀번호를 입력해 주세요.");
		}
		/** 필수 항목 체크 E */
		memId = memId.trim();
		memPw = memPw.trim();
		
		/** 아이디로 회원정보를 조회(아이디, 회원번호)  */
		Member member = get(memId);
		if (member == null) { // 회원정보가 존재 X 
			throw new AlertException("회원정보가 없습니다.");
		}
		
		/** 비밀번호 해시 체크 S */
		boolean match = BCrypt.checkpw(memPw, member.getMemPw());
		if (!match) { // 비밀번호가 일치 하지 않을 경우 
			throw new AlertException("비밀번호가 일치하지 않습니다.");
		}
		/** 비밀번호 해시 체크 E */
		
		/** 로그인 처리 S */
		HttpSession session = request.getSession();
		session.setAttribute("memNo", member.getMemNo());
		/** 로그인 처리 E */
	}
	
	public void login(HttpServletRequest request) throws AlertException {
		String memId = request.getParameter("memId");
		String memPw = request.getParameter("memPw");
		
		login(request, memId, memPw);
	}
	
	/**
	 * 회원 정보 조회 
	 * 
	 * @param memNo 회원번호
	 * @return
	 */
	public Member get(int memNo) {
		Member member = null;
		String sql = "SELECT * FROM member WHERE memNo = ?";
		try (Connection conn = DB.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, memNo);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				member = new Member(rs);
			}
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		}
 		
		return member;
	}
	
	/**
	 * 회원정보 조회
	 * 
	 * @param memId 회원 아이디 
	 * @return
	 */
	public Member get(String memId) {
		int memNo = 0;
		String sql = "SELECT memNo FROM member WHERE memId = ?";
		try(Connection conn = DB.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, memId);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				memNo = rs.getInt("memNo");
			}
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return get(memNo);
	}
}


