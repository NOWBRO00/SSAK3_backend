package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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

        // 판매자 온도 증가 (찜 추가 시 +0.1도)
        try {
            UserProfile seller = product.getSeller();
            if (seller != null) {
                // seller 초기화
                seller.getId();
                double currentTemperature = seller.getTemperature();
                double newTemperature = Math.min(currentTemperature + 0.1, 99.9); // 최대 99.9도
                seller.setTemperature(newTemperature);
                userRepository.save(seller);
                log.info("판매자 온도 증가: sellerId={}, {}도 -> {}도", seller.getId(), currentTemperature, newTemperature);
            }
        } catch (Exception e) {
            log.warn("판매자 온도 증가 중 오류 발생: productId={}, error={}", productId, e.getMessage());
            // 온도 증가 실패해도 찜 추가는 진행
        }

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

        // 판매자 온도 감소 (찜 취소 시 -0.1도)
        try {
            UserProfile seller = product.getSeller();
            if (seller != null) {
                // seller 초기화
                seller.getId();
                double currentTemperature = seller.getTemperature();
                double newTemperature = Math.max(currentTemperature - 0.1, 36.5); // 최소 36.5도
                seller.setTemperature(newTemperature);
                userRepository.save(seller);
                log.info("판매자 온도 감소: sellerId={}, {}도 -> {}도", seller.getId(), currentTemperature, newTemperature);
            }
        } catch (Exception e) {
            log.warn("판매자 온도 감소 중 오류 발생: productId={}, error={}", productId, e.getMessage());
            // 온도 감소 실패해도 찜 취소는 진행
        }

        likeRepository.delete(like);
    }

    //내 찜리스트 보기
    public List<Like> getUserLikes(Long userId) {
        try {
            log.info("찜 목록 조회 시작: userId={}", userId);
            // userId가 카카오 ID일 수도 있고, UserProfile의 id일 수도 있음
            UserProfile user = userRepository.findById(userId)
                    .orElse(null);
            
            // UserProfile의 id로 찾지 못했으면 카카오 ID로 시도
            if (user == null) {
                log.debug("UserProfile id로 사용자를 찾지 못함. kakaoId로 시도: {}", userId);
                user = userRepository.findByKakaoId(userId);
            }
            
            if (user == null) {
                log.warn("사용자를 찾을 수 없습니다. userId={} (UserProfile id 또는 kakaoId로 조회 실패)", userId);
                // 사용자가 없으면 빈 리스트 반환
                return new ArrayList<>();
            }
            
            log.debug("사용자 조회 성공: id={}, kakaoId={}, nickname={}", user.getId(), user.getKakaoId(), user.getNickname());
            List<Like> likes = likeRepository.findByUser(user);
            log.debug("찜 조회 완료: {}개", likes != null ? likes.size() : 0);
            // product, category, images, seller를 명시적으로 초기화하여 LazyInitializationException 방지
            if (likes != null && !likes.isEmpty()) {
                List<Like> validLikes = new ArrayList<>();
                likes.forEach(like -> {
                    try {
                        if (like.getProduct() != null) {
                            // 기본 필드 초기화
                            like.getProduct().getId();
                            like.getProduct().getTitle();
                            like.getProduct().getPrice();
                            like.getProduct().getDescription();
                            like.getProduct().getStatus();
                            
                            // seller 초기화
                            if (like.getProduct().getSeller() != null) {
                                like.getProduct().getSeller().getId();
                                like.getProduct().getSeller().getNickname();
                                like.getProduct().getSeller().getKakaoId();
                            }
                            
                            // category 초기화
                            if (like.getProduct().getCategory() != null) {
                                like.getProduct().getCategory().getId();
                                like.getProduct().getCategory().getName();
                            }
                            
                            // images 초기화
                            if (like.getProduct().getImages() != null) {
                                like.getProduct().getImages().size();
                                like.getProduct().getImages().forEach(img -> {
                                    if (img != null) {
                                        img.getId();
                                        img.getImageUrl();
                                        img.getOrderIndex();
                                    }
                                });
                            }
                            
                            validLikes.add(like);
                        }
                    } catch (Exception e) {
                        log.error("찜 초기화 중 오류: likeId={}, error={}", like.getId(), e.getMessage(), e);
                    }
                });
                log.info("유효한 찜 {}개 반환", validLikes.size());
                return validLikes;
            }
            log.info("찜이 없습니다. 빈 리스트 반환");
            return new ArrayList<>();
        } catch (Exception e) {
            // 예외 발생 시 빈 리스트 반환
            log.error("찜 목록 조회 중 오류 발생: userId={}, error={}", userId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    // 특정 사용자가 특정 상품을 찜했는지 확인
    public boolean isLiked(Long userId, Long productId) {
        try {
            // userId가 카카오 ID일 수도 있고, UserProfile의 id일 수도 있음
            UserProfile user = userRepository.findById(userId).orElse(null);
            
            if (user == null) {
                user = userRepository.findByKakaoId(userId);
            }
            
            if (user == null) {
                log.debug("사용자를 찾을 수 없습니다. userId={}", userId);
                return false;
            }
            
            Product product = productRepository.findById(productId).orElse(null);
            if (product == null) {
                log.debug("상품을 찾을 수 없습니다. productId={}", productId);
                return false;
            }
            
            return likeRepository.findByUserAndProduct(user, product).isPresent();
        } catch (Exception e) {
            log.warn("찜 확인 중 오류 발생: userId={}, productId={}, error={}", userId, productId, e.getMessage());
            return false;
        }
    }
}

