package org.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security 설정을 담당하는 구성 클래스
 * 
 * 주요 기능:
 * 1. HTTP 요청에 대한 인증/인가 규칙 설정
 * 2. CORS (Cross-Origin Resource Sharing) 설정
 * 3. CSRF 보호 설정
 * 4. 로그아웃 처리 설정
 * 
 * 현재 설정:
 * - 개발 환경을 위한 H2 콘솔 접근 허용
 * - API 엔드포인트에 대한 CORS 및 CSRF 설정
 * 
 * @author Ssak3 Backend Team
 * @version 1.0
 * @since 2024-10-05
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Spring Security 필터 체인을 구성합니다.
     * 
     * 이 메서드는 다음과 같은 보안 설정을 구성합니다:
     * 1. CORS 설정 적용
     * 2. HTTP 요청에 대한 인증/인가 규칙 설정
     * 3. 로그아웃 처리 설정
     * 4. CSRF 보호 설정
     * 5. 헤더 보안 설정
     * 
     * 인증이 필요하지 않은 경로:
     * - "/" (홈페이지)
     * - "/login/**" (로그인 관련 모든 경로)
     * - "/h2-console/**" (H2 데이터베이스 콘솔)
     * - "/api/**" (모든 API 엔드포인트)
     * 
     * @param http HttpSecurity 객체
     * @return SecurityFilterChain 구성된 보안 필터 체인
     * @throws Exception 설정 중 예외 발생 시
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        System.out.println("============ [SecurityConfig.java] SecurityConfig.filterChain() 시작 ============");
        System.out.println("입력 매개변수 - http: " + http.getClass().getSimpleName());
        
        http
            // CORS 설정 적용
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // HTTP 요청에 대한 인증/인가 규칙 설정
            .authorizeHttpRequests(authz -> authz
                // 인증 없이 접근 가능한 경로들
                .requestMatchers("/api/**").permitAll()        // API 전체 허용
                .requestMatchers("/h2-console/**").permitAll()  // H2 콘솔 (개발용)
                .requestMatchers("/", "/login/**").permitAll()  // 홈, 로그인 페이지
                .anyRequest().permitAll()                      // 나머지 모든 요청 허용 (개발용)
            )
            
            // 로그아웃 처리 설정
            .logout(logout -> logout
                .logoutSuccessUrl("/")           // 로그아웃 성공 시 홈페이지로 리다이렉트
                .invalidateHttpSession(true)     // 세션 무효화
                .deleteCookies("JSESSIONID")     // JSESSIONID 쿠키 삭제
            )
            
            // CSRF 보호 설정
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**", "/api/**")  // H2 콘솔과 API는 CSRF 보호 제외
            )
            
            // 헤더 보안 설정
            .headers(headers -> headers
                .frameOptions(frame -> frame.disable())  // H2 콘솔용 iframe 허용
            );

        SecurityFilterChain result = http.build();
        System.out.println("반환값: " + result.getClass().getSimpleName());
        System.out.println("============ [SecurityConfig.java] SecurityConfig.filterChain() 종료 ============");
        return result;
    }

    /**
     * CORS (Cross-Origin Resource Sharing) 설정을 구성합니다.
     * 
     * 이 설정은 다른 도메인에서의 요청을 허용하여 웹 애플리케이션의
     * 크로스 오리진 요청을 처리할 수 있도록 합니다.
     * 
     * 현재 설정:
     * - 모든 오리진 허용 (*)
     * - 모든 HTTP 메서드 허용 (GET, POST, PUT, DELETE, OPTIONS)
     * - 모든 헤더 허용
     * - 인증 정보 포함 허용
     * 
     * 보안 고려사항:
     * - 프로덕션 환경에서는 특정 도메인만 허용하도록 수정 필요
     * - 현재는 개발 환경을 위해 모든 오리진을 허용
     * 
     * @return CorsConfigurationSource CORS 설정이 적용된 구성 소스
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        System.out.println("============ [SecurityConfig.java] SecurityConfig.corsConfigurationSource() 시작 ============");
        System.out.println("입력 매개변수: 없음");
        
        // CORS 설정 객체 생성
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 허용할 오리진 설정 (현재는 모든 오리진 허용)
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        
        // 허용할 HTTP 메서드 설정
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // 허용할 헤더 설정 (현재는 모든 헤더 허용)
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // 인증 정보(쿠키, Authorization 헤더 등) 포함 허용
        configuration.setAllowCredentials(true);
        
        // URL 기반 CORS 구성 소스 생성
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        
        // 모든 경로(/**)에 CORS 설정 적용
        source.registerCorsConfiguration("/**", configuration);
        
        System.out.println("반환값: " + source.getClass().getSimpleName());
        System.out.println("============ [SecurityConfig.java] SecurityConfig.corsConfigurationSource() 종료 ============");
        return source;
    }
}
