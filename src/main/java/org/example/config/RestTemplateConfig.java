package org.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * 외부 API 호출을 위한 RestTemplate 설정.
 */
@Configuration
public class RestTemplateConfig {

	@Bean
	/**
	 * 애플리케이션 전역에서 재사용할 {@link RestTemplate} 빈을 생성합니다.
	 *
	 * <p>현재는 기본 설정만 사용하지만, 필요에 따라 커넥션 타임아웃, 인터셉터 등을
	 * 추가해 확장할 수 있습니다.</p>
	 */
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
}



