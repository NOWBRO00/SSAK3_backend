package org.likelion.hsu.ssak3.repository;

import org.likelion.hsu.ssak3.entity.Category;
import org.likelion.hsu.ssak3.entity.Product;
import org.likelion.hsu.ssak3.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategory(Category category);
    List<Product> findBySeller(UserProfile seller);
}
