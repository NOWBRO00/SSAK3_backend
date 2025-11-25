package org.example.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.KakaoUserInfoResponseDto;
import org.example.service.KakaoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 카카오 OAuth2 인증 콜백을 처리하는 컨트롤러 클래스
 * 
 * 주요 기능:
 * 1. 카카오 인증 완료 후 리다이렉트된 콜백 요청 처리
 * 2. 인가 코드를 사용하여 액세스 토큰 발급
 * 3. 액세스 토큰을 사용하여 사용자 정보 조회
 * 4. 사용자 정보를 JSON 형태로 반환
 * 
 * 카카오 OAuth2 콜백 플로우:
 * 1. 사용자가 카카오 로그인 완료
 * 2. 카카오가 인가 코드와 함께 이 컨트롤러의 /login/oauth2/code/kakao 엔드포인트로 리다이렉트
 * 3. 인가 코드를 추출하여 KakaoService를 통해 토큰 발급 및 사용자 정보 조회
 * 4. 조회된 사용자 정보를 JSON 응답으로 반환
 * 
 * @author Ssak3 Backend Team
 * @version 1.0
 * @since 2024-10-05
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class CallbackController {

    /**
     * 카카오 OAuth2 인증 및 사용자 정보 조회를 담당하는 서비스
     * @RequiredArgsConstructor로 자동 주입됨
     */
    private final KakaoService kakaoService;

    /**
     * 카카오 OAuth2 인증 콜백을 처리합니다.
     * 
     * 카카오 인증 플로우:
     * 1. 사용자가 카카오 로그인 페이지에서 인증 완료
     * 2. 카카오가 인가 코드(code)와 함께 이 엔드포인트로 리다이렉트
     * 3. 인가 코드를 사용하여 액세스 토큰 발급
     * 4. 액세스 토큰을 사용하여 사용자 정보 조회
     * 5. 사용자 정보를 JSON 형태로 반환
     * 
     * URL: GET /login/oauth2/code/kakao?code={authorization_code}
     * 
     * @param code 카카오 OAuth2 인증 완료 후 받은 인가 코드
     * @return ResponseEntity 사용자 정보가 담긴 JSON 응답 또는 에러 메시지
     *         - 성공 시: 200 OK + KakaoUserInfoResponseDto
     *         - 실패 시: 500 Internal Server Error + 에러 메시지
     */
    @GetMapping("/callback")
    public ResponseEntity<?> callback(@RequestParam("code") String code) {
        log.info("============ [CallbackController.java] callback() 시작 ============");
        log.info("입력 매개변수: code = {}", code);

        try {
            // 1단계: 인가 코드를 사용하여 카카오로부터 액세스 토큰 발급
            String accessToken = kakaoService.getAccessTokenFromKakao(code);
            log.info("받은 액세스 토큰: {}", accessToken);

            // 2단계: 액세스 토큰을 사용하여 카카오로부터 사용자 정보 조회
            KakaoUserInfoResponseDto userInfo = kakaoService.getUserInfo(accessToken);
            log.info("받은 사용자 정보: {}", userInfo);

            // 3단계: 서버 측 사용자 인증/인가 로직 (향후 확장 가능)
            // 예시:
            // - 데이터베이스에 사용자 정보 저장 또는 업데이트
            // - JWT 토큰 발급하여 세션 관리
            // - 사용자 권한 설정
            // - 로그인 이력 기록 등

            log.info("============ [CallbackController.java] callback() 종료 ============");
            
            // 성공 시 사용자 정보를 JSON 형태로 반환
            return new ResponseEntity<>(userInfo, HttpStatus.OK);
            
        } catch (Exception e) {
            // 예외 발생 시 에러 로그 기록 및 에러 응답 반환
            log.error("카카오 로그인 처리 실패: {}", e.getMessage(), e);
            return new ResponseEntity<>("로그인 처리 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
