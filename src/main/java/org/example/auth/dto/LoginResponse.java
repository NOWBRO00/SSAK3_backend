package org.example.auth.dto;

/**
 * 프론트엔드로 반환할 로그인 응답 DTO.
 */
public record LoginResponse(
		/**
		 * 우리 서비스에서 발급한 액세스 토큰.
		 */
		String accessToken,
		/**
		 * 우리 서비스에서 발급한 리프레시 토큰.
		 */
		String refreshToken,
		/**
		 * 로그인한 사용자의 프로필 정보.
		 */
		KakaoProfile profile,
		/**
		 * 백엔드 내부 사용자 ID (UserProfile.id).
		 * 프론트엔드는 이 ID를 userId로 사용해야 합니다.
		 */
		Long userId,
		/**
		 * 카카오 사용자 ID (UserProfile.kakaoId).
		 * 호환성을 위해 포함합니다.
		 */
		Long kakaoId
) {
}



