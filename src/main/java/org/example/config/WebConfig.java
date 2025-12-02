package org.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * Web MVC 설정 클래스
 * CORS 설정과 정적 리소스 설정을 구성합니다.
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

    /**
     * 업로드된 이미지 파일을 정적 리소스로 제공합니다.
     * /uploads/** 경로로 접근 가능하도록 설정합니다.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadDir = System.getProperty("user.dir") + File.separator + "uploads" + File.separator;
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir);
    }
}

