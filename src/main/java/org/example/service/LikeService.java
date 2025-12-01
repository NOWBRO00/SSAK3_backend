package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.entity.Like;
import org.example.entity.Product;
import org.example.entity.UserProfile;
import org.example.repository.LikeRepository;
import org.example.repository.ProductRepository;
import org.example.repository.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
        // userId가 카카오 ID일 수도 있고, UserProfile의 id일 수도 있음
        UserProfile user = userRepository.findById(userId)
                .orElse(null);
        
        // UserProfile의 id로 찾지 못했으면 카카오 ID로 시도
        if (user == null) {
            user = userRepository.findByKakaoId(userId);
        }
        
        if (user == null) {
            throw new IllegalArgumentException("존재하지 않는 유저입니다. userId: " + userId);
        }
        
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
        // userId가 카카오 ID일 수도 있고, UserProfile의 id일 수도 있음
        UserProfile user = userRepository.findById(userId)
                .orElse(null);
        
        // UserProfile의 id로 찾지 못했으면 카카오 ID로 시도
        if (user == null) {
            user = userRepository.findByKakaoId(userId);
        }
        
        if (user == null) {
            throw new IllegalArgumentException("존재하지 않는 유저입니다. userId: " + userId);
        }
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));

        Like like = likeRepository.findByUserAndProduct(user, product)
                .orElseThrow(() -> new IllegalArgumentException("찜하지 않은 상품입니다."));

        likeRepository.delete(like);
    }

    //내 찜리스트 보기
    public List<Like> getUserLikes(Long userId) {
        try {
            // userId가 카카오 ID일 수도 있고, UserProfile의 id일 수도 있음
            UserProfile user = userRepository.findById(userId)
                    .orElse(null);
            
            // UserProfile의 id로 찾지 못했으면 카카오 ID로 시도
            if (user == null) {
                user = userRepository.findByKakaoId(userId);
            }
            
            if (user == null) {
                // 사용자가 없으면 빈 리스트 반환
                return new ArrayList<>();
            }
            
            List<Like> likes = likeRepository.findByUser(user);
            // product, category, images를 명시적으로 초기화하여 LazyInitializationException 방지
            if (likes != null && !likes.isEmpty()) {
                likes.forEach(like -> {
                    try {
                        if (like.getProduct() != null) {
                            like.getProduct().getId();
                            like.getProduct().getTitle();
                            // category 초기화
                            if (like.getProduct().getCategory() != null) {
                                like.getProduct().getCategory().getId();
                                like.getProduct().getCategory().getName();
                            }
                            // images 초기화
                            if (like.getProduct().getImages() != null) {
                                like.getProduct().getImages().size();
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("찜 초기화 중 오류: " + e.getMessage());
                    }
                });
            }
            return likes != null ? likes : new ArrayList<>();
        } catch (Exception e) {
            // 예외 발생 시 빈 리스트 반환
            System.err.println("찜 목록 조회 중 오류: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}

