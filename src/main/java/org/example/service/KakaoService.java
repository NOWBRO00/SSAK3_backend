package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.KakaoUserInfoResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * 카카오 OAuth2 인증 및 사용자 정보 조회를 담당하는 서비스 클래스
 * 
 * 주요 기능:
 * 1. 인가 코드를 사용하여 카카오로부터 액세스 토큰 발급
 * 2. 액세스 토큰을 사용하여 카카오 사용자 정보 조회
 * 
 * 카카오 OAuth2 플로우:
 * 1. 사용자가 카카오 로그인 버튼 클릭
 * 2. 카카오 인증 서버로 리다이렉트
 * 3. 사용자 인증 완료 후 인가 코드와 함께 콜백 URL로 리다이렉트
 * 4. 인가 코드를 사용하여 액세스 토큰 요청 (이 클래스의 getAccessTokenFromKakao 메서드)
 * 5. 액세스 토큰을 사용하여 사용자 정보 요청 (이 클래스의 getUserInfo 메서드)
 * 
 * @author Ssak3 Backend Team
 * @version 1.0
 * @since 2024-10-05
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoService {

    /**
     * 카카오 개발자 콘솔에서 발급받은 애플리케이션의 Client ID
     * application.yml의 kakao.client_id 값으로 주입됨
     * 카카오 API 호출 시 클라이언트 식별을 위해 사용
     */
    @Value("${kakao.client_id}")
    private String clientId;

    /**
     * 카카오 개발자 콘솔에서 발급받은 애플리케이션의 Client Secret
     * application.yml의 kakao.client_secret 값으로 주입됨
     * 보안상 중요하므로 외부에 노출되지 않도록 주의
     * 액세스 토큰 발급 시 서버 인증을 위해 사용
     */
    @Value("${kakao.client_secret}")
    private String clientSecret;

    /**
     * 카카오 OAuth2 인증 완료 후 리다이렉트될 URI
     * application.yml의 kakao.redirect_uri 값으로 주입됨
     * 카카오 개발자 콘솔에 등록된 URI와 정확히 일치해야 함
     * 현재 설정: http://localhost:8080/login/oauth2/code/kakao
     */
    @Value("${kakao.redirect_uri}")
    private String redirectUri;

    /**
     * HTTP 클라이언트로 카카오 API 호출을 위한 RestTemplate
     * 카카오 토큰 발급 API와 사용자 정보 API 호출에 사용
     * 동기식 HTTP 통신을 위해 사용
     */
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 카카오로부터 받은 인가 코드를 사용하여 액세스 토큰을 발급받습니다.
     * 
     * 카카오 OAuth2 토큰 발급 API 호출:
     * - URL: https://kauth.kakao.com/oauth/token
     * - Method: POST
     * - Content-Type: application/x-www-form-urlencoded
     * 
     * 요청 파라미터:
     * - grant_type: authorization_code (고정값)
     * - client_id: 카카오 앱의 Client ID
     * - client_secret: 카카오 앱의 Client Secret (보안을 위해 필수)
     * - redirect_uri: 콜백 URI (인가 코드 요청 시와 동일해야 함)
     * - code: 카카오로부터 받은 인가 코드
     * 
     * @param code 카카오 OAuth2 인증 완료 후 받은 인가 코드
     * @return 카카오 API 호출에 사용할 액세스 토큰
     * @throws RuntimeException 토큰 발급 실패 시 예외 발생
     */
    public String getAccessTokenFromKakao(String code) {
        log.info("============ [KakaoService.java] getAccessTokenFromKakao() 시작 ============");
        log.info("입력 매개변수: code = {}", code);

        // 카카오 OAuth2 토큰 발급 API 엔드포인트
        String tokenUrl = "https://kauth.kakao.com/oauth/token";

        // HTTP 요청 헤더 설정
        // 카카오 API는 form-urlencoded 형식을 요구함
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // 토큰 발급 요청 파라미터 설정
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");  // OAuth2 인가 코드 플로우 사용
        params.add("client_id", clientId);               // 카카오 앱 식별자
        params.add("client_secret", clientSecret);       // 서버 인증을 위한 시크릿 키
        params.add("redirect_uri", redirectUri);         // 콜백 URI (인가 코드 요청 시와 동일)
        params.add("code", code);                        // 카카오로부터 받은 인가 코드

        // HTTP 요청 엔티티 생성 (헤더 + 바디)
        HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(params, headers);

        log.info("토큰 요청 URL: {}", tokenUrl);
        log.info("토큰 요청 파라미터: {}", params);

        try {
            // 카카오 토큰 발급 API 호출
            ResponseEntity<Map> response = restTemplate.exchange(
                tokenUrl,
                HttpMethod.POST,
                tokenRequest,
                Map.class
            );

            log.info("토큰 응답 상태: {}", response.getStatusCode());
            log.info("토큰 응답 바디: {}", response.getBody());

            // 응답에서 액세스 토큰 추출
            String accessToken = (String) response.getBody().get("access_token");
            log.info("추출된 액세스 토큰: {}", accessToken);
            log.info("============ [KakaoService.java] getAccessTokenFromKakao() 종료 ============");
            
            return accessToken;
        } catch (Exception e) {
            log.error("토큰 요청 실패: {}", e.getMessage(), e);
            throw new RuntimeException("카카오 토큰 요청 실패", e);
        }
    }

    /**
     * 액세스 토큰을 사용하여 카카오로부터 사용자 정보를 조회합니다.
     * 
     * 카카오 사용자 정보 API 호출:
     * - URL: https://kapi.kakao.com/v2/user/me
     * - Method: POST
     * - Authorization: Bearer {access_token}
     * 
     * 응답 데이터 구조:
     * {
     *   "id": 123456789,
     *   "kakao_account": {
     *     "profile": {
     *       "nickname": "사용자닉네임"
     *     }
     *   }
     * }
     * 
     * @param accessToken 카카오 API 호출에 사용할 액세스 토큰
     * @return 파싱된 사용자 정보를 담은 KakaoUserInfoResponseDto 객체
     * @throws RuntimeException 사용자 정보 조회 실패 시 예외 발생
     */
    public KakaoUserInfoResponseDto getUserInfo(String accessToken) {
        log.info("============ [KakaoService.java] getUserInfo() 시작 ============");
        log.info("입력 매개변수: accessToken = {}", accessToken);

        // 카카오 사용자 정보 조회 API 엔드포인트
        String userInfoUrl = "https://kapi.kakao.com/v2/user/me";

        // HTTP 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);  // Bearer 토큰 인증
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP 요청 엔티티 생성 (헤더만 필요, 바디는 없음)
        HttpEntity<MultiValueMap<String, String>> userInfoRequest = new HttpEntity<>(headers);

        log.info("사용자 정보 요청 URL: {}", userInfoUrl);
        log.info("사용자 정보 요청 헤더: {}", headers);

        try {
            // 카카오 사용자 정보 API 호출
            ResponseEntity<Map> response = restTemplate.exchange(
                userInfoUrl,
                HttpMethod.POST,
                userInfoRequest,
                Map.class
            );

            log.info("사용자 정보 응답 상태: {}", response.getStatusCode());
            log.info("사용자 정보 응답 바디: {}", response.getBody());

            // 응답 바디에서 사용자 정보 추출
            Map<String, Object> responseBody = response.getBody();
            
            // 카카오 사용자 고유 ID 추출
            Long id = Long.valueOf(responseBody.get("id").toString());
            
            // 카카오 계정 정보 추출
            Map<String, Object> kakaoAccount = (Map<String, Object>) responseBody.get("kakao_account");
            
            // 프로필 정보 추출
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            
            // 닉네임 추출
            String nickname = (String) profile.get("nickname");
            
            // DTO 객체 생성 및 반환
            KakaoUserInfoResponseDto userInfo = KakaoUserInfoResponseDto.builder()
                .id(id)           // 카카오 사용자 고유 ID
                .nickname(nickname)  // 사용자 닉네임
                .build();

            log.info("파싱된 사용자 정보: {}", userInfo);
            log.info("============ [KakaoService.java] getUserInfo() 종료 ============");
            
            return userInfo;
        } catch (Exception e) {
            log.error("사용자 정보 요청 실패: {}", e.getMessage(), e);
            throw new RuntimeException("카카오 사용자 정보 요청 실패", e);
        }
    }
}
