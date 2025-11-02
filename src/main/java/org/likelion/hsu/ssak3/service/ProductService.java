package org.likelion.hsu.ssak3.service;

import org.likelion.hsu.ssak3.entity.*;
import org.likelion.hsu.ssak3.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserProfileRepository userProfileRepository;
    private final ProductImageRepository productImageRepository;

    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    //JSON 요청용 (이미지 없이)
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    // form-data + 이미지 업로드용
    public Product createProduct(
            String title,
            int price,
            String description,
            Long categoryId,
            Long sellerId,
            List<MultipartFile> images
    ) throws IOException {

        // 1️⃣ 카테고리 & 판매자 조회
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다."));
        UserProfile seller = userProfileRepository.findById(sellerId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 2️⃣ 상품 엔티티 생성 및 저장
        Product product = new Product();
        product.setTitle(title);
        product.setPrice(price);
        product.setDescription(description);
        product.setStatus(ProductStatus.ON_SALE);
        product.setCategory(category);
        product.setSeller(seller);

        Product savedProduct = productRepository.save(product);

        // 3️⃣ 이미지 처리
        if (images != null && !images.isEmpty()) {
            List<ProductImage> imageEntities = new ArrayList<>();

            for (int i = 0; i < images.size(); i++) {
                MultipartFile file = images.get(i);
                if (!file.isEmpty()) {
                    // 파일명 생성
                    String uniqueName = UUID.randomUUID() + "_" + file.getOriginalFilename();

                    // 폴더 생성
                    File uploadDir = new File(UPLOAD_DIR);
                    if (!uploadDir.exists()) uploadDir.mkdirs();

                    // 실제 파일 저장
                    File destination = new File(UPLOAD_DIR + uniqueName);
                    file.transferTo(destination);

                    // DB에 이미지 정보 저장
                    ProductImage image = ProductImage.builder()
                            .product(savedProduct)
                            .imageUrl("/uploads/" + uniqueName)
                            .orderIndex(i)
                            .build();

                    productImageRepository.save(image);
                    imageEntities.add(image);
                }
            }

            // 4️⃣ Product에 이미지 세팅
            savedProduct.setImages(imageEntities);
        }

        return savedProduct;
    }

    // 전체 상품 조회
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // 상품 상세조회
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    // 카테고리별 조회
    public List<Product> getProductsByCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다."));
        return productRepository.findByCategory(category);
    }

    // 판매자별 조회
    public List<Product> getProductsBySeller(Long sellerId) {
        UserProfile seller = userProfileRepository.findById(sellerId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        return productRepository.findBySeller(seller);
    }

    // 상품 수정
    public Product updateProduct(Long id, Product updated) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));
        product.setTitle(updated.getTitle());
        product.setDescription(updated.getDescription());
        product.setPrice(updated.getPrice());
        product.setStatus(updated.getStatus());
        return productRepository.save(product);
    }

    // 상품 삭제
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}
