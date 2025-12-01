package org.example.auth.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.example.auth.dto.AuthCodeRequest;
import org.example.auth.dto.LoginResponse;
import org.example.auth.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 인증 관련 HTTP 요청을 처리하는 컨트롤러입니다.
 *
 * <p>현재는 카카오 OAuth 로그인만 지원하지만, 향후 다른 인증 수단도 이 컨트롤러에서
 * 확장할 수 있습니다.</p>
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

	private static final Logger log = LoggerFactory.getLogger(AuthController.class);

	private final AuthService authService;

	@PostMapping("/kakao")
	/**
	 * 카카오 인가 코드를 받아 로그인 처리를 위임합니다.
	 *
	 * @param request 카카오 인가 코드 정보를 담은 요청 본문
	 * @return 액세스 토큰, 리프레시 토큰, 사용자 프로필을 포함한 응답
	 */
	public ResponseEntity<LoginResponse> loginWithKakao(@Valid @RequestBody AuthCodeRequest request) {
		log.info("POST /api/auth/kakao 호출 - code={} (처음 20자: {})", 
				request.code(), 
				request.code() != null && request.code().length() > 20 
					? request.code().substring(0, 20) + "..." 
					: request.code());
		try {
			LoginResponse response = authService.loginWithKakao(request.code());
			log.info("카카오 로그인 성공 - 사용자 ID: {}", response.profile().id());
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			log.error("카카오 로그인 처리 중 오류 발생 - code={}", request.code(), e);
			throw e;
		}
	}
}

