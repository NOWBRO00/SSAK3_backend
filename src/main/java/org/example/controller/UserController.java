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

    // 특정 유저 조회
    @GetMapping("/{id}")
    public ResponseEntity<UserProfile> getUserById(@PathVariable Long id) {
        try {
            return userRepository.findById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("유저 조회 중 오류 발생: userId={}", id, e);
            return ResponseEntity.notFound().build();
        }
    }

    // 카카오 ID로 유저 조회
    @GetMapping("/kakao/{kakaoId}")
    public ResponseEntity<UserProfile> getUserByKakaoId(@PathVariable Long kakaoId) {
        try {
            UserProfile user = userRepository.findByKakaoId(kakaoId);
            if (user != null) {
                return ResponseEntity.ok(user);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("유저 조회 중 오류 발생: kakaoId={}", kakaoId, e);
            return ResponseEntity.notFound().build();
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


