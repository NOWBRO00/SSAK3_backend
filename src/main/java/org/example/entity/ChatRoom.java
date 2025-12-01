package org.example.entity;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
}

