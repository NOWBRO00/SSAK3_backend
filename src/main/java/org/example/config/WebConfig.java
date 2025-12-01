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
        // 환경 변수로 Netlify 도메인을 받을 수 있도록 설정
        String netlifyDomain = System.getenv("NETLIFY_DOMAIN");
        if (netlifyDomain == null || netlifyDomain.isEmpty()) {
            netlifyDomain = "https://fancy-tanuki-129c30.netlify.app";
        }

        registry.addMapping("/**")
                .allowedOrigins(
                        "http://localhost:3000",
                        "http://localhost:5173",
                        netlifyDomain
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}

