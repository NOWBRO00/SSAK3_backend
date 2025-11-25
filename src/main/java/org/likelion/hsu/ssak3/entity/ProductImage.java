package org.likelion.hsu.ssak3.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "product_images")
public class ProductImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ 어떤 상품에 속한 이미지인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonBackReference    // ✅ 순환참조 반대쪽 (Product → ProductImage → Product 무한루프 방지)
    private Product product;

    // ✅ 이미지의 URL (S3 또는 서버 업로드 경로)
    @Column(nullable = false)
    private String imageUrl;

    // ✅ 이미지 순서 (썸네일, 보조 이미지 등 구분 가능)
    @Column(name = "image_order")
    private int orderIndex;
}
