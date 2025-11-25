package org.likelion.hsu.ssak3.service;

import lombok.RequiredArgsConstructor;
import org.likelion.hsu.ssak3.entity.Like;
import org.likelion.hsu.ssak3.entity.Product;
import org.likelion.hsu.ssak3.entity.UserProfile;
import org.likelion.hsu.ssak3.repository.LikeRepository;
import org.likelion.hsu.ssak3.repository.ProductRepository;
import org.likelion.hsu.ssak3.repository.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class LikeService {

    private final LikeRepository likeRepository;
    private final UserProfileRepository userRepository;
    private final ProductRepository productRepository;

    //찜 추가
    public Like addLike(Long userId, Long productId) {
        UserProfile user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));

        // 이미 찜한 상품인지 확인
        likeRepository.findByUserAndProduct(user, product)
                .ifPresent(like -> {
                    throw new IllegalStateException("이미 찜한 상품입니다.");
                });

        Like like = Like.builder()
                .user(user)
                .product(product)
                .build();

        return likeRepository.save(like);
    }

    // 찜 취소
    public void removeLike(Long userId, Long productId) {
        UserProfile user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));

        Like like = likeRepository.findByUserAndProduct(user, product)
                .orElseThrow(() -> new IllegalArgumentException("찜하지 않은 상품입니다."));

        likeRepository.delete(like);
    }

    //내 찜리스트 보기
    public List<Like> getUserLikes(Long userId) {
        UserProfile user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
        return likeRepository.findByUser(user);
    }
}
