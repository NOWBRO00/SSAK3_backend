package org.example.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 카카오 토큰 발급 API 응답을 매핑하는 DTO입니다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record KakaoTokenResponse(
		/**
		 * 토큰 타입 (일반적으로 "bearer").
		 */
		@JsonProperty("token_type")
		String tokenType,

		/**
		 * 액세스 토큰 값.
		 */
		@JsonProperty("access_token")
		String accessToken,

		/**
		 * 액세스 토큰 만료까지 남은 시간(초).
		 */
		@JsonProperty("expires_in")
		Long expiresIn,

		/**
		 * 리프레시 토큰 값.
		 */
		@JsonProperty("refresh_token")
		String refreshToken,

		/**
		 * 리프레시 토큰 만료까지 남은 시간(초).
		 */
		@JsonProperty("refresh_token_expires_in")
		Long refreshTokenExpiresIn,

		/**
		 * 부여된 권한 범위.
		 */
		@JsonProperty("scope")
		String scope
) {
}



