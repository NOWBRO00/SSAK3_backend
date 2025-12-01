package org.example.auth.client;

import java.net.URI;
import java.util.Objects;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.RequestEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.util.Assert;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import org.example.auth.dto.KakaoTokenResponse;
import org.example.auth.dto.KakaoUserResponse;
import org.example.auth.exception.KakaoApiException;
import org.example.config.KakaoProperties;

import lombok.RequiredArgsConstructor;

/**
 * 카카오 OAuth API와의 통신을 담당합니다.
 */
@Component
@RequiredArgsConstructor
public class KakaoOAuthClient {

	private static final Logger log = LoggerFactory.getLogger(KakaoOAuthClient.class);

	private final RestTemplate restTemplate;
	private final KakaoProperties kakaoProperties;

	/**
	 * 카카오 OAuth 서버에 인가 코드를 전달해 액세스/리프레시 토큰을 요청합니다.
	 *
	 * @param code 카카오에서 발급한 인가 코드
	 * @return 카카오 토큰 응답 DTO
	 * @throws KakaoApiException 카카오 API가 오류를 반환한 경우
	 */
	public KakaoTokenResponse requestToken(String code) {
		Assert.hasText(code, "카카오 인가 코드는 비어 있을 수 없습니다.");

		String clientId = kakaoProperties.getClientId();
		String clientSecret = kakaoProperties.getClientSecret();
		String redirectUri = kakaoProperties.getRedirectUri();

		log.info("카카오 토큰 요청 파라미터 확인 - clientId={}, redirectUri={}, secretSet={}",
				clientId,
				redirectUri,
				StringUtils.hasText(clientSecret));

		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

			MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
			body.add("grant_type", "authorization_code");
			body.add("client_id", clientId);
			body.add("redirect_uri", redirectUri);
			body.add("code", code);

			if (StringUtils.hasText(clientSecret)) {
				body.add("client_secret", clientSecret);
			}

			HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);
			String tokenUri = Objects.requireNonNull(
					kakaoProperties.getTokenUri(),
					"카카오 토큰 URI가 설정되지 않았습니다."
			);

			ResponseEntity<KakaoTokenResponse> response = restTemplate.postForEntity(
					tokenUri,
					requestEntity,
					KakaoTokenResponse.class
			);

			return Objects.requireNonNull(response.getBody(), "카카오 토큰 응답이 비어있습니다.");
		} catch (HttpStatusCodeException ex) {
			String responseBody = ex.getResponseBodyAsString();
			log.error("카카오 토큰 발급 API 호출 실패. status={}, body={}, clientId={}, redirectUri={}", 
					ex.getStatusCode(), responseBody, clientId, redirectUri, ex);
			
			// 401 오류의 경우 더 자세한 정보 로깅
			if (ex.getStatusCode().value() == 401) {
				log.error("401 오류 가능 원인: 1) Redirect URI 불일치 2) Client ID/Secret 불일치 3) 카카오 개발자 콘솔 설정 확인 필요");
				log.error("요청 파라미터 - grant_type={}, client_id={}, redirect_uri={}, code={}", 
						"authorization_code", clientId, redirectUri, code);
			}
			
			String reason = String.format(
					"카카오 토큰 발급 요청이 실패했습니다. status=%s, body=%s",
					ex.getStatusCode(),
					StringUtils.hasText(responseBody) ? responseBody : "null"
			);
			throw new KakaoApiException(reason, ex);
		}
	}

	/**
	 * 카카오에서 제공하는 사용자 정보 API를 호출합니다.
	 *
	 * @param accessToken 카카오 액세스 토큰
	 * @return 카카오 사용자 프로필 응답 DTO
	 * @throws KakaoApiException 카카오 API가 오류를 반환한 경우
	 */
	public KakaoUserResponse requestUserProfile(String accessToken) {
		try {
			HttpHeaders headers = new HttpHeaders();
			String bearerToken = Objects.requireNonNull(accessToken, "카카오 액세스 토큰은 비어 있을 수 없습니다.");
			Assert.hasText(bearerToken, "카카오 액세스 토큰은 비어 있을 수 없습니다.");
			headers.setBearerAuth(bearerToken);

			String userInfoUri = Objects.requireNonNull(
					kakaoProperties.getUserInfoUri(),
					"카카오 사용자 정보 URI가 설정되지 않았습니다."
			);
			Assert.hasText(userInfoUri, "카카오 사용자 정보 URI가 설정되지 않았습니다.");
			URI userInfoEndpoint = Objects.requireNonNull(
					URI.create(userInfoUri),
					"카카오 사용자 정보 URI를 생성할 수 없습니다."
			);

			RequestEntity<Void> request = RequestEntity
					.get(userInfoEndpoint)
					.headers(headers)
					.build();

			ResponseEntity<KakaoUserResponse> response = restTemplate.exchange(request, KakaoUserResponse.class);

			return Objects.requireNonNull(response.getBody(), "카카오 사용자 정보 응답이 비어있습니다.");
		} catch (HttpStatusCodeException ex) {
			String responseBody = ex.getResponseBodyAsString();
			String reason = String.format(
					"카카오 사용자 정보 조회가 실패했습니다. status=%s, body=%s",
					ex.getStatusCode(),
					StringUtils.hasText(responseBody) ? responseBody : "null"
			);
			log.error("카카오 사용자 정보 API 호출 실패. status={}, body={}", ex.getStatusCode(), responseBody, ex);
			throw new KakaoApiException(reason, ex);
		}
	}
}

