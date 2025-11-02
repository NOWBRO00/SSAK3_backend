package org.likelion.hsu.ssak3.repository;

import org.likelion.hsu.ssak3.entity.Like;
import org.likelion.hsu.ssak3.entity.Product;
import org.likelion.hsu.ssak3.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    List<Like> findByUser(UserProfile user);
    List<Like> findByProduct(Product product);
}
