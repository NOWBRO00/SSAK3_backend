package org.example.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.KakaoUserInfoResponseDto;
import org.example.service.KakaoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 카카오 로그인 페이지를 제공하는 컨트롤러 클래스
 * 
 * 주요 기능:
 * 1. 카카오 로그인 페이지 표시
 * 2. 카카오 OAuth2 인증 URL 생성 및 제공
 * 3. Thymeleaf 템플릿을 통한 동적 URL 생성
 * 
 * 카카오 OAuth2 인증 URL 구조:
 * https://kauth.kakao.com/oauth/authorize?
 *   response_type=code&
 *   client_id={CLIENT_ID}&
 *   redirect_uri={REDIRECT_URI}
 * 
 * @author Ssak3 Backend Team
 * @version 1.0
 * @since 2024-10-05
 */
@Controller
@RequestMapping("/login")
@RequiredArgsConstructor
@Slf4j
public class KakaoLoginController {

    /**
     * 카카오 개발자 콘솔에서 발급받은 애플리케이션의 Client ID
     * application.yml의 kakao.client_id 값으로 주입됨
     * 카카오 OAuth2 인증 URL 생성 시 사용
     */
    @Value("${kakao.client_id}")
    private String clientId;

    /**
     * 카카오 OAuth2 인증 완료 후 리다이렉트될 URI
     * application.yml의 kakao.redirect_uri 값으로 주입됨
     * 카카오 OAuth2 인증 URL 생성 시 사용
     * 현재 설정: http://localhost:8080/login/oauth2/code/kakao
     */
    @Value("${kakao.redirect_uri}")
    private String redirectUri;

    /**
     * 카카오 OAuth2 인증 및 사용자 정보 조회를 담당하는 서비스
     * @RequiredArgsConstructor로 자동 주입됨
     * (현재는 사용하지 않지만 향후 확장 가능)
     */
    private final KakaoService kakaoService;

    /**
     * 카카오 로그인 페이지를 표시합니다.
     * 
     * 이 메서드는 다음과 같은 작업을 수행합니다:
     * 1. 카카오 OAuth2 인증 URL을 동적으로 생성
     * 2. 생성된 URL을 Thymeleaf 모델에 추가
     * 3. login.html 템플릿을 반환하여 사용자에게 카카오 로그인 버튼 제공
     * 
     * 생성되는 카카오 인증 URL 예시:
     * https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=00f734cbba1d0989b814e95429aaefcd&redirect_uri=http://localhost:8080/login/oauth2/code/kakao
     * 
     * URL: GET /login/page
     * 
     * @param model Thymeleaf 템플릿에 데이터를 전달하기 위한 Model 객체
     * @return String "login" - login.html 템플릿 파일명
     */
    @GetMapping("/page")
    public String loginPage(Model model) {
        log.info("============ [KakaoLoginController.java] loginPage() 시작 ============");
        log.info("입력 매개변수: model = {}", model);

        // 카카오 OAuth2 인증 URL 생성
        // 카카오 인증 서버로 리다이렉트할 URL을 동적으로 생성
        String location = "https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=" + 
                         clientId + "&redirect_uri=" + redirectUri;
        
        // Thymeleaf 템플릿에서 사용할 수 있도록 모델에 URL 추가
        model.addAttribute("location", location);
        
        log.info("생성된 카카오 로그인 URL: {}", location);
        log.info("============ [KakaoLoginController.java] loginPage() 종료 ============");
        
        // login.html 템플릿을 반환하여 카카오 로그인 페이지 표시
        return "login";
    }

}
