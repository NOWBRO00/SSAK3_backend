package org.example.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * 카카오 OAuth 설정 값을 바인딩하는 프로퍼티 클래스입니다.
 * 환경 변수 또는 application.yml에서 설정을 읽습니다.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "kakao")
public class KakaoProperties {

	/**
	 * 카카오 REST API 키 (client_id).
	 * 환경 변수: KAKAO_CLIENT_ID
	 */
	@Value("${kakao.client-id:${KAKAO_CLIENT_ID:}}")
	private String clientId;

	/**
	 * 카카오 클라이언트 시크릿 (client_secret).
	 * 환경 변수: KAKAO_CLIENT_SECRET
	 * <p>카카오 개발자 콘솔에서 비활성화한 경우 비워둘 수 있습니다.</p>
	 */
	@Value("${kakao.client-secret:${KAKAO_CLIENT_SECRET:}}")
	private String clientSecret;

	/**
	 * 인가 코드 승인 후 리다이렉트될 URI.
	 * 환경 변수: KAKAO_REDIRECT_URI
	 */
	@Value("${kakao.redirect-uri:${KAKAO_REDIRECT_URI:https://fancy-tanuki-129c30.netlify.app/auth/kakao/callback}}")
	private String redirectUri;

	/**
	 * 카카오 OAuth 토큰 발급 엔드포인트 URI.
	 * 환경 변수: KAKAO_TOKEN_URI
	 */
	@Value("${kakao.token-uri:${KAKAO_TOKEN_URI:https://kauth.kakao.com/oauth/token}}")
	private String tokenUri;

	/**
	 * 카카오 사용자 정보 조회 엔드포인트 URI.
	 * 환경 변수: KAKAO_USER_INFO_URI
	 */
	@Value("${kakao.user-info-uri:${KAKAO_USER_INFO_URI:https://kapi.kakao.com/v2/user/me}}")
	private String userInfoUri;

}



