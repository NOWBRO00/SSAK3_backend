package org.example.repository;

import org.example.entity.Like;
import org.example.entity.Product;
import org.example.entity.UserProfile;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    //특정 유저가 찜한 상품 목록
    @EntityGraph(attributePaths = {"product", "product.images"})
    List<Like> findByUser(UserProfile user);

    //특정 상품을 찜한 유저 목록
    @EntityGraph(attributePaths = {"user"})
    List<Like> findByProduct(Product product);

    //찜추가/삭제할때 확인하는기능
    Optional<Like> findByUserAndProduct(UserProfile user, Product product);
}



