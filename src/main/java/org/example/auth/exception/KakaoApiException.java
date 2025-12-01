package org.example.auth.exception;

/**
 * 카카오 API 연동 중 발생한 예외를 표현합니다.
 */
public class KakaoApiException extends RuntimeException {

	/**
	 * 카카오 API 호출 과정의 오류를 나타내는 생성자입니다.
	 *
	 * @param message 예외 메시지
	 */
	public KakaoApiException(String message) {
		super(message);
	}

	/**
	 * 카카오 API 호출 예외의 상세 원인을 함께 전달합니다.
	 *
	 * @param message 예외 메시지
	 * @param cause   원인이 된 예외
	 */
	public KakaoApiException(String message, Throwable cause) {
		super(message, cause);
	}
}



