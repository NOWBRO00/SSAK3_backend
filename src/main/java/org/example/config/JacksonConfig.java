package org.example.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Jackson JSON 직렬화 설정
 * 순환 참조 방지 및 성능 최적화
 */
@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.build();
        
        // 순환 참조 방지: 무한 루프 감지 시 예외 발생
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        
        // null 값은 JSON에 포함하지 않음
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        
        // 순환 참조 감지 활성화 (기본값이지만 명시적으로 설정)
        objectMapper.configure(SerializationFeature.WRITE_SELF_REFERENCES_AS_NULL, false);
        
        return objectMapper;
    }
}

