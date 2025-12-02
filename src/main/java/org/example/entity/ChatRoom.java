package org.example.entity;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "chat_rooms", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"buyer_id", "seller_id", "product_id"})
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ChatRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 구매자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    @JsonIgnore
    private UserProfile buyer;

    // 판매자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    @JsonIgnore
    private UserProfile seller;

    // 관련 상품
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore
    private Product product;

    // 메시지 목록
    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private List<Message> messages = new ArrayList<>();

    // 프론트엔드가 기대하는 형식에 맞추기 위한 getter 메서드들
    @JsonGetter("buyerId")
    public Long getBuyerId() {
        return buyer != null ? buyer.getId() : null;
    }

    @JsonGetter("sellerId")
    public Long getSellerId() {
        return seller != null ? seller.getId() : null;
    }

    @JsonGetter("productId")
    public Long getProductId() {
        return product != null ? product.getId() : null;
    }

    @JsonGetter("buyer")
    public UserProfile getBuyerForJson() {
        return buyer;
    }

    @JsonGetter("seller")
    public UserProfile getSellerForJson() {
        return seller;
    }

    @JsonGetter("product")
    public Product getProductForJson() {
        return product;
    }

    // 최근 메시지 정보 (채팅방 목록에서 사용)
    @JsonGetter("lastMessage")
    public Message getLastMessage() {
        try {
            if (messages == null || messages.isEmpty()) {
                return null;
            }
            // 생성 시간 기준으로 최신 메시지 반환
            return messages.stream()
                    .max((m1, m2) -> {
                        if (m1.getCreatedAt() == null && m2.getCreatedAt() == null) return 0;
                        if (m1.getCreatedAt() == null) return -1;
                        if (m2.getCreatedAt() == null) return 1;
                        return m1.getCreatedAt().compareTo(m2.getCreatedAt());
                    })
                    .orElse(null);
        } catch (Exception e) {
            // LazyInitializationException 등 예외 발생 시 null 반환
            return null;
        }
    }

    // 읽지 않은 메시지 수 (현재 사용자 기준으로 계산 필요 - 서비스 레이어에서 처리)
    @JsonGetter("unreadCount")
    public Long getUnreadCount() {
        // 이 필드는 서비스 레이어에서 동적으로 설정해야 함
        // 현재는 null 반환 (서비스에서 별도로 계산)
        return null;
    }
}

