package org.example.auth.service;

import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import org.example.auth.client.KakaoOAuthClient;
import org.example.auth.dto.KakaoProfile;
import org.example.auth.dto.KakaoTokenResponse;
import org.example.auth.dto.KakaoUserResponse;
import org.example.auth.dto.LoginResponse;
import org.example.auth.dto.TokenPair;
import org.example.entity.UserProfile;
import org.example.repository.UserProfileRepository;

import lombok.RequiredArgsConstructor;

/**
 * 카카오 OAuth 기반 인증 흐름을 캡슐화한 서비스입니다.
 *
 * <p>컨트롤러로부터 인가 코드를 전달받아 토큰 발급 및 사용자 프로필 조회를 수행하고,
 * 서비스용 토큰을 생성해 반환합니다.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

	private static final Logger log = LoggerFactory.getLogger(AuthService.class);

	private final KakaoOAuthClient kakaoOAuthClient;
	private final UserProfileRepository userProfileRepository;

	/**
	 * 카카오 인가 코드를 기반으로 로그인 절차를 수행합니다.
	 *
	 * @param code 카카오에서 발급한 인가 코드
	 * @return 액세스 토큰/리프레시 토큰 및 사용자 프로필 정보를 포함한 응답
	 */
	public LoginResponse loginWithKakao(String code) {
		log.info("카카오 로그인 서비스 시작 - code={}", code);
		KakaoTokenResponse tokenResponse = kakaoOAuthClient.requestToken(code);
		log.info("카카오 토큰 발급 성공 - accessToken={}, refreshToken={}", tokenResponse.accessToken(), tokenResponse.refreshToken());
		KakaoUserResponse userResponse = kakaoOAuthClient.requestUserProfile(tokenResponse.accessToken());
		log.info("카카오 사용자 정보 조회 성공 - id={}", userResponse.id());

		KakaoProfile profile = toProfile(userResponse);
		
		// UserProfile 저장 또는 업데이트
		UserProfile userProfile = saveOrUpdateUserProfile(profile);
		log.info("사용자 프로필 저장/업데이트 완료 - userId={}, kakaoId={}", userProfile.getId(), userProfile.getKakaoId());
		
		TokenPair tokens = issueToken(profile);

		log.info("임시 토큰 발급 완료 - accessToken={}, refreshToken={}", tokens.accessToken(), tokens.refreshToken());
		// 로그인 응답에 내부 userId와 kakaoId 포함
		return new LoginResponse(
				tokens.accessToken(), 
				tokens.refreshToken(), 
				profile,
				userProfile.getId(),      // 백엔드 내부 userId
				userProfile.getKakaoId()  // 카카오 ID (호환성)
		);
	}
	
	/**
	 * 카카오 프로필 정보를 기반으로 UserProfile을 저장하거나 업데이트합니다.
	 * 
	 * @param profile 카카오 프로필 정보
	 * @return 저장/업데이트된 UserProfile
	 */
	private UserProfile saveOrUpdateUserProfile(KakaoProfile profile) {
		try {
			log.info("사용자 저장/업데이트 시작 - kakaoId={}, nickname={}", profile.id(), profile.nickname());
			
			// 기존 사용자 찾기 (카카오 ID로)
			UserProfile userProfile = userProfileRepository.findByKakaoId(profile.id());
			log.debug("기존 사용자 조회 결과 - kakaoId={}, 존재여부={}", profile.id(), userProfile != null);
			
			if (userProfile == null) {
				// 새 사용자 생성
				log.info("새 사용자 생성 시작 - kakaoId={}, nickname={}", profile.id(), profile.nickname());
				userProfile = new UserProfile();
				userProfile.setKakaoId(profile.id());
				userProfile.setNickname(profile.nickname() != null ? profile.nickname() : "카카오 사용자");
				userProfile.setProfileImage(profile.profileImageUrl() != null ? profile.profileImageUrl() : profile.thumbnailImageUrl());
				userProfile.setTemperature(36.5); // 기본 매너온도
				log.info("새 사용자 엔티티 생성 완료 - kakaoId={}, nickname={}", userProfile.getKakaoId(), userProfile.getNickname());
			} else {
				// 기존 사용자 정보 업데이트
				log.info("기존 사용자 발견 - userId={}, kakaoId={}, nickname={}", 
						userProfile.getId(), userProfile.getKakaoId(), userProfile.getNickname());
				userProfile.setNickname(profile.nickname() != null ? profile.nickname() : userProfile.getNickname());
				if (profile.profileImageUrl() != null || profile.thumbnailImageUrl() != null) {
					userProfile.setProfileImage(profile.profileImageUrl() != null ? profile.profileImageUrl() : profile.thumbnailImageUrl());
				}
				log.info("기존 사용자 정보 업데이트 준비 완료");
			}
			
			// 사용자 저장
			log.info("데이터베이스에 사용자 저장 시작 - kakaoId={}", profile.id());
			UserProfile saved = userProfileRepository.save(userProfile);
			log.info("데이터베이스에 사용자 저장 완료 - userId={}, kakaoId={}, nickname={}", 
					saved.getId(), saved.getKakaoId(), saved.getNickname());
			
			// 저장 확인
			UserProfile verify = userProfileRepository.findById(saved.getId()).orElse(null);
			if (verify == null) {
				log.error("사용자 저장 후 조회 실패 - userId={}, kakaoId={}", saved.getId(), saved.getKakaoId());
				throw new RuntimeException("사용자 저장 후 조회에 실패했습니다.");
			}
			log.info("사용자 저장 검증 완료 - userId={}, kakaoId={}", verify.getId(), verify.getKakaoId());
			
			return saved;
		} catch (Exception e) {
			log.error("사용자 저장/업데이트 중 오류 발생 - kakaoId={}, error={}", profile.id(), e.getMessage(), e);
			throw new RuntimeException("사용자 저장 중 오류가 발생했습니다: " + e.getMessage(), e);
		}
	}

	/**
	 * 카카오 사용자 응답을 우리 서비스에서 사용하는 프로필 객체로 변환합니다.
	 *
	 * @param userResponse 카카오 사용자 정보 응답
	 * @return 변환된 {@link KakaoProfile}
	 */
	private KakaoProfile toProfile(KakaoUserResponse userResponse) {
		String nickname = Optional.ofNullable(userResponse)
				.map(KakaoUserResponse::kakaoAccount)
				.map(KakaoUserResponse.KakaoAccount::profile)
				.map(KakaoUserResponse.Profile::nickname)
				.filter(StringUtils::hasText)
				.orElse("카카오 사용자");

		String profileImage = Optional.ofNullable(userResponse)
				.map(KakaoUserResponse::kakaoAccount)
				.map(KakaoUserResponse.KakaoAccount::profile)
				.map(KakaoUserResponse.Profile::profileImageUrl)
				.orElse(null);

		String thumbnailImage = Optional.ofNullable(userResponse)
				.map(KakaoUserResponse::kakaoAccount)
				.map(KakaoUserResponse.KakaoAccount::profile)
				.map(KakaoUserResponse.Profile::thumbnailImageUrl)
				.orElse(null);

		String email = Optional.ofNullable(userResponse)
				.map(KakaoUserResponse::kakaoAccount)
				.map(KakaoUserResponse.KakaoAccount::email)
				.filter(StringUtils::hasText)
				.orElse(null);

		return new KakaoProfile(userResponse.id(), nickname, email, profileImage, thumbnailImage);
	}

	/**
	 * 우리 서비스에서 사용할 임시 토큰을 발급합니다.
	 *
	 * <p>현재는 데모용으로 UUID 기반 토큰을 생성하고 있으며,
	 * 운영 환경에서는 JWT 등 실사용 토큰 발급 로직으로 교체해야 합니다.</p>
	 *
	 * @param profile 로그인한 사용자 프로필
	 * @return 발급된 액세스/리프레시 토큰 정보
	 */
	private TokenPair issueToken(KakaoProfile profile) {
		// TODO: 운영 환경에서는 JWT 또는 세션 기반 토큰 발급 로직으로 교체하세요.
		return new TokenPair(
				"access-" + UUID.randomUUID(),
				"refresh-" + UUID.randomUUID()
		);
	}
}

