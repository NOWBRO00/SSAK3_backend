package org.likelion.hsu.ssak3.repository;

import org.likelion.hsu.ssak3.entity.Like;
import org.likelion.hsu.ssak3.entity.Product;
import org.likelion.hsu.ssak3.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    //특정 유저가 찜한 상품 목록
    List<Like> findByUser(UserProfile user);

    //특정 상품을 찜한 유저 목록
    List<Like> findByProduct(Product product);

    //찜추가/삭제할때 확인하는기능
    Optional<Like> findByUserAndProduct(UserProfile user, Product product);
}
