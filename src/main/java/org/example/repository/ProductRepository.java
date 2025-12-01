package org.example.repository;

import org.example.entity.Category;
import org.example.entity.Product;
import org.example.entity.UserProfile;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // @EntityGraph 제거 - 빈 데이터베이스에서도 안전하게 동작하도록
    @Override
    List<Product> findAll();
    
    @EntityGraph(attributePaths = {"images", "seller", "category"})
    @Override
    java.util.Optional<Product> findById(Long id);
    
    @EntityGraph(attributePaths = {"images", "seller", "category"})
    List<Product> findByCategory(Category category);
    
    @EntityGraph(attributePaths = {"images", "seller", "category"})
    List<Product> findBySeller(UserProfile seller);
}



