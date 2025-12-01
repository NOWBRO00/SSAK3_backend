package org.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 설정 클래스
 * CORS 설정을 추가로 구성합니다.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(
                        "http://localhost:*",                       // 로컬 개발 환경 (모든 포트)
                        "https://*.netlify.app",                    // Netlify 모든 서브도메인
                        "https://fancy-tanuki-129c30.netlify.app",  // Netlify 배포 환경
                        "https://ssak3-backend.onrender.com"        // Render 백엔드 도메인
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}

