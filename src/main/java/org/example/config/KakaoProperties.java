package org.example.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * 카카오 OAuth 설정 값을 바인딩하는 프로퍼티 클래스입니다.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "kakao")
public class KakaoProperties {

	/**
	 * 카카오 REST API 키 (client_id).
	 */
	private String clientId;

	/**
	 * 카카오 클라이언트 시크릿 (client_secret).
	 * <p>카카오 개발자 콘솔에서 비활성화한 경우 비워둘 수 있습니다.</p>
	 */
	private String clientSecret;

	/**
	 * 인가 코드 승인 후 리다이렉트될 URI.
	 */
	private String redirectUri;

	/**
	 * 카카오 OAuth 토큰 발급 엔드포인트 URI.
	 */
	private String tokenUri;

	/**
	 * 카카오 사용자 정보 조회 엔드포인트 URI.
	 */
	private String userInfoUri;

}



