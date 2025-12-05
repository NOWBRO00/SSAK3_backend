package org.example.entity;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "products")
@JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler", "seller", "category", "images"}, ignoreUnknown = true)
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 판매자 정보 (순환참조 방지)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    @JsonIgnore
    private UserProfile seller;

    // 카테고리 정보 (순환참조 방지)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @JsonIgnore
    private Category category;

    // 프론트엔드가 기대하는 형식에 맞추기 위한 getter 메서드
    @JsonGetter("category")
    public Category getCategoryForJson() {
        return category;
    }

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private int price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ProductStatus status = ProductStatus.ON_SALE;

    // 이미지 목록
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore  // images 필드는 직접 직렬화하지 않고, imageUrls getter를 통해 제공
    @Builder.Default
    private List<ProductImage> images = new ArrayList<>();

    // 프론트엔드가 기대하는 형식에 맞추기 위한 getter 메서드들
    @JsonGetter("sellerId")
    public Long getSellerId() {
        return seller != null ? seller.getId() : null;
    }

    @JsonGetter("sellerNickname")
    public String getSellerNickname() {
        return seller != null ? seller.getNickname() : null;
    }

    @JsonGetter("sellerKakaoId")
    public Long getSellerKakaoId() {
        return seller != null ? seller.getKakaoId() : null;
    }

    @JsonGetter("categoryName")
    public String getCategoryName() {
        return category != null ? category.getName() : null;
    }

    @JsonGetter("imageUrls")
    public List<String> getImageUrls() {
        if (images == null || images.isEmpty()) {
            return new ArrayList<>();
        }
        return images.stream()
                .sorted((a, b) -> Integer.compare(a.getOrderIndex(), b.getOrderIndex()))
                .map(ProductImage::getImageUrl)
                .collect(Collectors.toList());
    }

    // 찜 상태 (프론트엔드에서 사용, DB에 저장되지 않음)
    @Transient
    @Builder.Default
    private Boolean isLiked = false;

    @JsonGetter("isLiked")
    public Boolean getIsLiked() {
        return isLiked != null ? isLiked : false;
    }

    public void setIsLiked(Boolean isLiked) {
        this.isLiked = isLiked;
    }
}

