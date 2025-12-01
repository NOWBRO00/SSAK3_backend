package org.example.auth.dto;

/**
 * 서비스에서 활용할 사용자 프로필 DTO.
 */
public record KakaoProfile(
		/**
		 * 카카오 사용자 식별자.
		 */
		Long id,
		/**
		 * 사용자 닉네임.
		 */
		String nickname,
		/**
		 * 사용자 이메일 (동의받은 경우에만 제공).
		 */
		String email,
		/**
		 * 프로필 이미지 URL.
		 */
		String profileImageUrl,
		/**
		 * 썸네일 이미지 URL.
		 */
		String thumbnailImageUrl
) {
}



