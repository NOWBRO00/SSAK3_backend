package org.example.repository;

import org.example.entity.ChatRoom;
import org.example.entity.Product;
import org.example.entity.UserProfile;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    
    // 구매자와 판매자, 상품으로 채팅방 찾기
    @EntityGraph(attributePaths = {"buyer", "seller", "product"})
    Optional<ChatRoom> findByBuyerAndSellerAndProduct(
            UserProfile buyer, 
            UserProfile seller, 
            Product product
    );

    // 사용자가 참여한 채팅방 목록 조회 (구매자 또는 판매자)
    // messages는 서비스 레이어에서 명시적으로 초기화 (순환 참조 방지)
    @EntityGraph(attributePaths = {"buyer", "seller", "product", "product.seller", "product.category", "product.images"})
    List<ChatRoom> findByBuyerOrSeller(UserProfile buyer, UserProfile seller);

    // 특정 사용자의 채팅방 목록 조회 (구매자)
    @EntityGraph(attributePaths = {"buyer", "seller", "product"})
    List<ChatRoom> findByBuyer(UserProfile buyer);

    // 특정 사용자의 채팅방 목록 조회 (판매자)
    @EntityGraph(attributePaths = {"buyer", "seller", "product"})
    List<ChatRoom> findBySeller(UserProfile seller);
}



