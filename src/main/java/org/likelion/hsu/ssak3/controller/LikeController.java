package org.likelion.hsu.ssak3.controller;

import lombok.RequiredArgsConstructor;
import org.likelion.hsu.ssak3.entity.Like;
import org.likelion.hsu.ssak3.service.LikeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    // 찜 추가
    @PostMapping
    public ResponseEntity<Like> addLike(@RequestParam Long userId, @RequestParam Long productId) {
        Like savedLike = likeService.addLike(userId, productId);
        return ResponseEntity.ok(savedLike);
    }

    // 찜 취소
    @DeleteMapping
    public ResponseEntity<String> removeLike(@RequestParam Long userId, @RequestParam Long productId) {
        likeService.removeLike(userId, productId);
        return ResponseEntity.ok("찜이 취소되었습니다.");
    }

    // 내 찜리스트 보기
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Like>> getUserLikes(@PathVariable Long userId) {
        List<Like> likes = likeService.getUserLikes(userId);
        return ResponseEntity.ok(likes);
    }
}
