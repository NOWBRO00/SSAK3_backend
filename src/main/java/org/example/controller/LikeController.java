package org.example.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.Like;
import org.example.service.LikeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    // 찜 추가
    @PostMapping
    public ResponseEntity<Like> addLike(@RequestParam Long userId, @RequestParam Long productId) {
        try {
            Like savedLike = likeService.addLike(userId, productId);
            return ResponseEntity.ok(savedLike);
        } catch (IllegalArgumentException e) {
            log.error("찜 추가 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            log.error("찜 추가 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // 찜 취소
    @DeleteMapping
    public ResponseEntity<String> removeLike(@RequestParam Long userId, @RequestParam Long productId) {
        try {
            likeService.removeLike(userId, productId);
            return ResponseEntity.ok("찜이 취소되었습니다.");
        } catch (IllegalArgumentException e) {
            log.error("찜 취소 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // 내 찜리스트 보기
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Like>> getUserLikes(@PathVariable Long userId) {
        try {
            List<Like> likes = likeService.getUserLikes(userId);
            log.info("찜 목록 조회 성공: userId={}, count={}", userId, likes != null ? likes.size() : 0);
            return ResponseEntity.ok(likes != null ? likes : new ArrayList<>());
        } catch (IllegalArgumentException e) {
            log.error("찜 목록 조회 실패: userId={}, error={}", userId, e.getMessage());
            // 사용자가 없어도 빈 리스트 반환
            return ResponseEntity.ok(new ArrayList<>());
        } catch (Exception e) {
            log.error("찜 목록 조회 중 오류 발생: userId={}", userId, e);
            return ResponseEntity.ok(new ArrayList<>()); // 빈 리스트 반환
        }
    }
}

