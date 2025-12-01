package org.example.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 프론트엔드에서 전달하는 카카오 인가 코드 요청 DTO.
 */
public record AuthCodeRequest(
		/**
		 * 카카오에서 전달된 인가 코드.
		 */
		@NotBlank(message = "인가 코드는 필수입니다.")
		String code
) {
}



