package org.example.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * 애플리케이션의 기본 페이지들을 처리하는 컨트롤러 클래스
 * 
 * 주요 기능:
 * 1. 홈페이지 표시
 * 2. 로그인 페이지로 리다이렉트
 * 3. 사용자 정보 API 제공
 * 4. 헬스 체크 API 제공
 * 
 * @author Ssak3 Backend Team
 * @version 1.0
 * @since 2024-10-05
 */
@Controller
public class HomeController {

    /**
     * 애플리케이션의 홈페이지를 표시합니다.
     * 
     * 현재는 단순한 홈페이지만 제공하며, 향후 로그인된 사용자 정보나
     * 애플리케이션 메인 기능들을 표시할 수 있습니다.
     * 
     * URL: GET /
     * 
     * @param model Thymeleaf 템플릿에 데이터를 전달하기 위한 Model 객체
     * @return String "home" - home.html 템플릿 파일명
     */
    @GetMapping("/")
    public String home(Model model) {
        System.out.println("============ [HomeController.java] HomeController.home() 시작 ============");
        System.out.println("입력 매개변수 - model: " + model);
        
        // REST API 방식에서는 세션 또는 JWT로 사용자 정보를 관리
        // 현재는 단순히 홈페이지만 표시
        // 향후 확장 가능: 로그인된 사용자 정보, 메뉴, 알림 등
        String result = "home";
        System.out.println("반환값: " + result);
        System.out.println("============ [HomeController.java] HomeController.home() 종료 ============");
        return result;
    }

    /**
     * 로그인 요청을 카카오 로그인 페이지로 리다이렉트합니다.
     * 
     * 사용자가 /login URL에 접근하면 카카오 로그인 페이지로 리다이렉트됩니다.
     * 실제 카카오 로그인 페이지는 KakaoLoginController에서 처리됩니다.
     * 
     * URL: GET /login
     * 
     * @return String "redirect:/login/page" - 카카오 로그인 페이지로 리다이렉트
     */
    @GetMapping("/login")
    public String login() {
        System.out.println("============ [HomeController.java] HomeController.login() 시작 ============");
        System.out.println("입력 매개변수: 없음");
        
        // 카카오 로그인 페이지로 리다이렉트
        // KakaoLoginController의 /login/page 엔드포인트로 이동
        String result = "redirect:/login/page";
        System.out.println("반환값: " + result);
        System.out.println("============ [HomeController.java] HomeController.login() 종료 ============");
        return result;
    }

    /**
     * 사용자 정보를 JSON 형태로 반환합니다.
     * 
     * 현재는 REST API 방식으로 구현되었다는 메시지만 반환합니다.
     * 향후 실제 사용자 정보를 반환하도록 확장할 수 있습니다.
     * 
     * URL: GET /user
     * 
     * @return Map<String, Object> 사용자 정보 또는 상태 메시지가 담긴 JSON 응답
     */
    @GetMapping("/user")
    @ResponseBody
    public Map<String, Object> user() {
        System.out.println("============ [HomeController.java] HomeController.user() 시작 ============");
        System.out.println("입력 매개변수: 없음");
        
        // REST API 방식에서는 세션 또는 JWT로 사용자 정보를 관리
        // 현재는 단순히 메시지만 반환
        // 향후 확장 가능: 실제 사용자 정보, 권한, 프로필 등
        Map<String, Object> result = Map.of("message", "REST API 방식으로 구현됨", "status", "OK");
        System.out.println("반환값: " + result);
        System.out.println("============ [HomeController.java] HomeController.user() 종료 ============");
        return result;
    }

    /**
     * 애플리케이션의 헬스 체크를 수행합니다.
     * 
     * 애플리케이션이 정상적으로 실행 중인지 확인하는 API입니다.
     * 로드 밸런서나 모니터링 시스템에서 사용할 수 있습니다.
     * 
     * URL: GET /api/health
     * 
     * @return Map<String, String> 헬스 체크 결과가 담긴 JSON 응답
     */
    @GetMapping("/api/health")
    @ResponseBody
    public Map<String, String> health() {
        System.out.println("============ [HomeController.java] HomeController.health() 시작 ============");
        System.out.println("입력 매개변수: 없음");
        
        // 애플리케이션 상태 확인
        // 향후 확장 가능: 데이터베이스 연결 상태, 외부 API 연결 상태 등
        Map<String, String> result = Map.of("status", "OK", "message", "Ssak3 Backend is running!");
        System.out.println("반환값: " + result);
        System.out.println("============ [HomeController.java] HomeController.health() 종료 ============");
        return result;
    }
}
