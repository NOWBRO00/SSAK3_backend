package org.example.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 카카오 사용자 정보 API 응답을 매핑하는 DTO입니다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record KakaoUserResponse(
		/**
		 * 카카오 회원 번호.
		 */
		Long id,

		/**
		 * 계정 정보 (이메일, 프로필 등).
		 */
		@JsonProperty("kakao_account")
		KakaoAccount kakaoAccount
) {

		/**
		 * 카카오 계정 정보 중 주요 필드를 담는 중첩 DTO입니다.
		 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record KakaoAccount(
			/**
			 * 사용자의 이메일.
			 */
			String email,

			/**
			 * 프로필 관련 정보.
			 */
			@JsonProperty("profile")
			Profile profile
	) {
	}

		/**
		 * 프로필 이미지와 닉네임 정보를 담는 DTO입니다.
		 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Profile(
			/**
			 * 사용자 닉네임.
			 */
			String nickname,

			/**
			 * 프로필 원본 이미지 URL.
			 */
			@JsonProperty("profile_image_url")
			String profileImageUrl,

			/**
			 * 프로필 썸네일 이미지 URL.
			 */
			@JsonProperty("thumbnail_image_url")
			String thumbnailImageUrl
	) {
	}
}



