package org.example.auth.exception;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 애플리케이션 전역에서 발생하는 예외를 처리하는 핸들러입니다.
 *
 * <p>컨트롤러 단에서 던져진 예외를 공통 포맷으로 변환해 프론트엔드에 전달합니다.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	/**
	 * 요청 값 검증 실패 시 발생하는 예외를 처리합니다.
	 *
	 * @param ex 검증 실패 예외
	 * @return 오류 코드 및 메시지를 담은 응답
	 */
	public ResponseEntity<ProblemDetailResponse> handleValidationException(MethodArgumentNotValidException ex) {
		String message = ex.getBindingResult()
				.getFieldErrors()
				.stream()
				.map(FieldError::getDefaultMessage)
				.collect(Collectors.joining(", "));

		return ResponseEntity.badRequest()
				.body(new ProblemDetailResponse("VALIDATION_ERROR", message));
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	/**
	 * 필수 요청 파라미터가 누락된 경우를 처리합니다.
	 *
	 * @param ex 누락된 파라미터 예외
	 * @return 오류 코드 및 메시지를 담은 응답
	 */
	public ResponseEntity<ProblemDetailResponse> handleMissingParameterException(MissingServletRequestParameterException ex) {
		String message = "필수 파라미터가 누락되었습니다: " + ex.getParameterName();
		return ResponseEntity.badRequest()
				.body(new ProblemDetailResponse("MISSING_PARAMETER", message));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	/**
	 * 잘못된 인자로 인한 예외를 처리합니다.
	 *
	 * @param ex 잘못된 인자 예외
	 * @return 오류 코드 및 메시지를 담은 응답
	 */
	public ResponseEntity<ProblemDetailResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
		return ResponseEntity.badRequest()
				.body(new ProblemDetailResponse("INVALID_ARGUMENT", ex.getMessage()));
	}

	@ExceptionHandler(Exception.class)
	/**
	 * 정의되지 않은 모든 예외를 500 응답으로 변환합니다.
	 *
	 * @param ex 발생한 예외
	 * @return 내부 서버 오류 응답
	 */
	public ResponseEntity<ProblemDetailResponse> handleGeneralException(Exception ex) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new ProblemDetailResponse("INTERNAL_ERROR", ex.getMessage()));
	}

	public record ProblemDetailResponse(
			/**
			 * 에러 유형 코드.
			 */
			String code,
			/**
			 * 사용자에게 전달할 메시지.
			 */
			String message
	) {
	}
}

