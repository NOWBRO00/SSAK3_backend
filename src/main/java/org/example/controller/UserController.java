package org.example.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.UserProfile;
import org.example.repository.UserProfileRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserProfileRepository userRepository;

    // 유저 등록
    @PostMapping
    public ResponseEntity<UserProfile> createUser(@RequestBody UserProfile user) {
        try {
            // 입력 검증
            if (user.getKakaoId() == null) {
                log.error("유저 등록 실패: 카카오 ID가 비어있습니다.");
                return ResponseEntity.badRequest().build();
            }
            if (user.getNickname() == null || user.getNickname().trim().isEmpty()) {
                log.error("유저 등록 실패: 닉네임이 비어있습니다.");
                return ResponseEntity.badRequest().build();
            }

            UserProfile saved = userRepository.save(user);
            log.info("유저 등록 성공: userId={}, kakaoId={}, nickname={}", 
                    saved.getId(), saved.getKakaoId(), saved.getNickname());
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            log.error("유저 등록 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // 전체 유저 조회
    @GetMapping
    public ResponseEntity<List<UserProfile>> getAllUsers() {
        try {
            List<UserProfile> users = userRepository.findAll();
            log.info("유저 조회 성공: {}개", users != null ? users.size() : 0);
            return ResponseEntity.ok(users != null ? users : new ArrayList<>());
        } catch (Exception e) {
            log.error("유저 조회 중 오류 발생", e);
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    // 특정 유저 조회 (id 또는 kakaoId로 조회)
    @GetMapping("/{id}")
    public ResponseEntity<UserProfile> getUserById(@PathVariable Long id) {
        try {
            log.info("유저 조회 요청: id={}", id);
            
            // 먼저 UserProfile의 id로 조회 시도
            UserProfile user = userRepository.findById(id).orElse(null);
            
            // id로 찾지 못했으면 kakaoId로 시도
            if (user == null) {
                log.debug("UserProfile id로 사용자를 찾지 못함. kakaoId로 시도: {}", id);
                user = userRepository.findByKakaoId(id);
            }
            
            if (user != null) {
                log.info("유저 조회 성공: userId={}, kakaoId={}, nickname={}", user.getId(), user.getKakaoId(), user.getNickname());
                return ResponseEntity.ok(user);
            } else {
                log.warn("유저를 찾을 수 없습니다: id={} (UserProfile id 또는 kakaoId로 조회 실패)", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("유저 조회 중 오류 발생: id={}", id, e);
            return ResponseEntity.notFound().build();
        }
    }

    // 카카오 ID로 유저 조회
    @GetMapping("/kakao/{kakaoId}")
    public ResponseEntity<UserProfile> getUserByKakaoId(@PathVariable Long kakaoId) {
        try {
            log.info("카카오 ID로 유저 조회 요청: kakaoId={}", kakaoId);
            UserProfile user = userRepository.findByKakaoId(kakaoId);
            if (user != null) {
                log.info("유저 조회 성공: userId={}, kakaoId={}, nickname={}", user.getId(), user.getKakaoId(), user.getNickname());
                return ResponseEntity.ok(user);
            } else {
                log.warn("유저를 찾을 수 없습니다: kakaoId={}", kakaoId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("유저 조회 중 오류 발생: kakaoId={}", kakaoId, e);
            return ResponseEntity.notFound().build();
        }
    }

    // 현재 로그인한 사용자 정보 조회 (카카오 ID를 쿼리 파라미터 또는 헤더로 받음)
    @GetMapping("/me")
    public ResponseEntity<UserProfile> getCurrentUser(
            @RequestParam(required = false) Long kakaoId,
            @RequestParam(required = false) Long userId,
            @RequestHeader(value = "X-Kakao-Id", required = false) Long kakaoIdFromHeader
    ) {
        try {
            // 쿼리 파라미터 또는 헤더에서 kakaoId 가져오기
            Long targetKakaoId = kakaoId != null ? kakaoId : kakaoIdFromHeader;
            
            // kakaoId가 없으면 userId로 조회 시도
            UserProfile user = null;
            if (targetKakaoId != null) {
                log.info("현재 사용자 조회 요청: kakaoId={}", targetKakaoId);
                user = userRepository.findByKakaoId(targetKakaoId);
            } else if (userId != null) {
                log.info("현재 사용자 조회 요청: userId={}", userId);
                // userId로 조회 시도 (내부 ID 또는 kakaoId일 수 있음)
                user = userRepository.findById(userId).orElse(null);
                if (user == null) {
                    user = userRepository.findByKakaoId(userId);
                }
            }
            
            if (user != null) {
                log.info("현재 사용자 조회 성공: userId={}, kakaoId={}, nickname={}, temperature={}", 
                        user.getId(), user.getKakaoId(), user.getNickname(), user.getTemperature());
                return ResponseEntity.ok(user);
            } else {
                log.warn("현재 사용자 조회 실패: kakaoId 또는 userId가 제공되지 않았거나 사용자를 찾을 수 없습니다. kakaoId={}, userId={}", 
                        targetKakaoId, userId);
                // 400 대신 404 반환 (사용자를 찾을 수 없음)
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("현재 사용자 조회 중 오류 발생: kakaoId={}, userId={}", 
                    kakaoId != null ? kakaoId : kakaoIdFromHeader, userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // 유저 수정
    @PutMapping("/{id}")
    public ResponseEntity<UserProfile> updateUser(@PathVariable Long id, @RequestBody UserProfile user) {
        try {
            UserProfile existingUser = userRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

            if (user.getNickname() != null) {
                existingUser.setNickname(user.getNickname());
            }
            if (user.getProfileImage() != null) {
                existingUser.setProfileImage(user.getProfileImage());
            }
            if (user.getTemperature() > 0) {
                existingUser.setTemperature(user.getTemperature());
            }

            UserProfile updated = userRepository.save(existingUser);
            log.info("유저 수정 성공: userId={}, nickname={}", updated.getId(), updated.getNickname());
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            log.error("유저 수정 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("유저 수정 중 오류 발생: userId={}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // 유저 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        try {
            if (!userRepository.existsById(id)) {
                log.error("유저 삭제 실패: 유저를 찾을 수 없습니다. userId={}", id);
                return ResponseEntity.notFound().build();
            }

            userRepository.deleteById(id);
            log.info("유저 삭제 성공: userId={}", id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("유저 삭제 중 오류 발생: userId={}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}


