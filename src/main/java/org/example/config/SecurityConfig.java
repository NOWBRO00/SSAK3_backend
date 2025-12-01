package org.example.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

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
            // CORS 설정 적용 (CSRF 설정 전에 적용)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // HTTP 요청에 대한 인증/인가 규칙 설정
            .authorizeHttpRequests(authz -> authz
                // OPTIONS 요청은 항상 허용 (CORS preflight)
                .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
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
                // OPTIONS 요청은 authorizeHttpRequests에서 permitAll()로 설정되어 있어 자동으로 CSRF 우회됨
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
     * CORS 필터를 SecurityFilterChain보다 먼저 실행되도록 등록합니다.
     * 
     * Render 환경에서 OPTIONS 요청이 SecurityFilterChain까지 도달하지 못하는 경우를 해결합니다.
     * 이 필터는 가장 높은 우선순위(Order 0)로 설정되어 모든 요청에 먼저 적용됩니다.
     * 
     * @return FilterRegistrationBean CORS 필터 등록 빈
     */
    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        // 와일드카드 패턴 사용 (credentials와 호환)
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*");  // 모든 오리진 허용 (패턴 사용)
        config.addAllowedHeader("*");         // 모든 헤더 허용
        config.addAllowedMethod("*");         // 모든 메서드 허용
        config.setMaxAge(3600L);              // Preflight 캐시 시간
        
        source.registerCorsConfiguration("/**", config);
        
        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);  // SecurityFilterChain보다 먼저 실행
        return bean;
    }

    /**
     * CORS (Cross-Origin Resource Sharing) 설정을 구성합니다.
     * 
     * SecurityFilterChain에서 사용되는 CORS 설정입니다.
     * 
     * @return CorsConfigurationSource CORS 설정이 적용된 구성 소스
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // ✅ setAllowedOriginPatterns() 사용 (Spring Security 6 권장)
        // 와일드카드 패턴 사용으로 Render 환경에서 안정적으로 동작
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:*",                              // 로컬 개발 환경 (모든 포트)
            "https://*.netlify.app",                           // Netlify 모든 서브도메인
            "https://fancy-tanuki-129c30.netlify.app",        // Netlify 배포 환경
            "https://ssak3-backend.onrender.com"              // Render 백엔드 도메인
        ));
        
        // 허용할 HTTP 메서드 설정
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));
        
        // 허용할 헤더 설정 (모든 헤더 허용)
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // 인증 정보(쿠키, Authorization 헤더 등) 포함 허용
        configuration.setAllowCredentials(true);
        
        // Preflight 요청 캐시 시간 (초)
        configuration.setMaxAge(3600L);
        
        // URL 기반 CORS 구성 소스 생성
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        
        // 모든 경로(/**)에 CORS 설정 적용
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}
