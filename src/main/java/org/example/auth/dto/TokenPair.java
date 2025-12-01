package org.example.auth.dto;

/**
 * 내부 인증 토큰 페어를 표현하는 DTO입니다.
 */
public record TokenPair(
		/**
		 * 발급된 액세스 토큰.
		 */
		String accessToken,
		/**
		 * 발급된 리프레시 토큰.
		 */
		String refreshToken
) {
}



