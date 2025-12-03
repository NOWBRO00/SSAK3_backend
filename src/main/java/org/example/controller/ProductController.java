package org.example.controller;

import org.example.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.Product;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    // form-data + 이미지 업로드
    @PostMapping("/with-upload")
    public ResponseEntity<Product> createProductWithUpload(
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "price", required = false) Integer price,
            @RequestParam(value = "description", required = false, defaultValue = "") String description,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "sellerId", required = false) Long sellerId,
            @RequestParam(value = "images", required = false) List<MultipartFile> images
    ) {
        try {
            // 입력 검증 및 로깅
            log.info("상품 등록 요청 받음 - title: {}, price: {}, categoryId: {}, sellerId: {}, images: {}", 
                    title, price, categoryId, sellerId, images != null ? images.size() : 0);
            
            if (title == null || title.trim().isEmpty()) {
                String errorMsg = "제목이 비어있습니다.";
                log.error("상품 등록 실패: {}", errorMsg);
                return ResponseEntity.badRequest()
                        .body((Product) null);
            }
            if (price == null || price <= 0) {
                String errorMsg = "가격이 0 이하이거나 null입니다. price=" + price;
                log.error("상품 등록 실패: {}", errorMsg);
                return ResponseEntity.badRequest()
                        .body((Product) null);
            }
            if (categoryId == null || categoryId <= 0) {
                String errorMsg = "유효하지 않은 카테고리 ID입니다. categoryId=" + categoryId;
                log.error("상품 등록 실패: {}", errorMsg);
                return ResponseEntity.badRequest()
                        .body((Product) null);
            }
            if (sellerId == null || sellerId <= 0) {
                String errorMsg = "유효하지 않은 판매자 ID입니다. sellerId=" + sellerId;
                log.error("상품 등록 실패: {}", errorMsg);
                return ResponseEntity.badRequest()
                        .body((Product) null);
            }

            Product saved = productService.createProduct(title, price.intValue(), description, categoryId, sellerId, images);
            log.info("상품 등록 성공: productId={}, title={}, sellerId={}", saved.getId(), title, sellerId);
        return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            log.error("상품 등록 실패 (IllegalArgumentException): {}", e.getMessage(), e);
            // IllegalArgumentException은 GlobalExceptionHandler에서 처리됨
            throw e;
        } catch (IOException e) {
            log.error("상품 등록 중 이미지 업로드 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body((Product) null);
        } catch (Exception e) {
            log.error("상품 등록 중 오류 발생: {}", e.getMessage(), e);
            e.printStackTrace();
            return ResponseEntity.status(500).body((Product) null);
        }
    }

    // JSON 요청용 (이미지 없이)
    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        try {
            // 입력 검증
            if (product.getTitle() == null || product.getTitle().trim().isEmpty()) {
                log.error("상품 등록 실패: 제목이 비어있습니다.");
                return ResponseEntity.badRequest().build();
            }
            if (product.getPrice() <= 0) {
                log.error("상품 등록 실패: 가격이 0 이하입니다. price={}", product.getPrice());
                return ResponseEntity.badRequest().build();
            }
            if (product.getCategory() == null || product.getCategory().getId() == null) {
                log.error("상품 등록 실패: 카테고리가 지정되지 않았습니다.");
                return ResponseEntity.badRequest().build();
            }
            if (product.getSeller() == null || product.getSeller().getId() == null) {
                log.error("상품 등록 실패: 판매자가 지정되지 않았습니다.");
                return ResponseEntity.badRequest().build();
            }

        Product saved = productService.createProduct(product);
            log.info("상품 등록 성공: productId={}, title={}", saved.getId(), saved.getTitle());
        return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            log.error("상품 등록 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("상품 등록 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // 전체 상품 조회
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        try {
            log.info("GET /api/products 요청 받음");
            List<Product> products = productService.getAllProducts();
            log.info("상품 조회 성공: {}개", products != null ? products.size() : 0);
            return ResponseEntity.ok(products != null ? products : new ArrayList<>());
        } catch (Exception e) {
            log.error("상품 조회 중 오류 발생: {}", e.getMessage(), e);
            e.printStackTrace();
            // 에러가 발생해도 빈 리스트를 반환하여 500 에러 방지
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    // 상품 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 카테고리별 조회
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable Long categoryId) {
        try {
            List<Product> products = productService.getProductsByCategory(categoryId);
            log.info("카테고리별 상품 조회 성공: categoryId={}, count={}", categoryId, products != null ? products.size() : 0);
            return ResponseEntity.ok(products != null ? products : new ArrayList<>());
        } catch (IllegalArgumentException e) {
            log.error("카테고리별 상품 조회 실패: categoryId={}, error={}", categoryId, e.getMessage());
            throw e; // GlobalExceptionHandler에서 처리
        } catch (Exception e) {
            log.error("카테고리별 상품 조회 중 오류 발생: categoryId={}", categoryId, e);
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    // 판매자별 조회
    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<List<Product>> getProductsBySeller(@PathVariable Long sellerId) {
        try {
            List<Product> products = productService.getProductsBySeller(sellerId);
            log.info("판매자별 상품 조회 성공: sellerId={}, count={}", sellerId, products != null ? products.size() : 0);
            return ResponseEntity.ok(products != null ? products : new ArrayList<>());
        } catch (Exception e) {
            log.error("판매자별 상품 조회 중 오류 발생: sellerId={}", sellerId, e);
            return ResponseEntity.ok(new ArrayList<>()); // 빈 리스트 반환
        }
    }

    // 키워드로 상품 검색
    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String keyword) {
        try {
            log.info("상품 검색 요청: keyword={}", keyword);
            List<Product> products = productService.searchProducts(keyword);
            log.info("상품 검색 성공: keyword={}, count={}", keyword, products != null ? products.size() : 0);
            return ResponseEntity.ok(products != null ? products : new ArrayList<>());
        } catch (Exception e) {
            log.error("상품 검색 중 오류 발생: keyword={}, error={}", keyword, e.getMessage(), e);
            e.printStackTrace();
            // 에러가 발생해도 빈 리스트를 반환하여 500 에러 방지
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    // 상품 수정
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        try {
            Product updated = productService.updateProduct(id, product);
            log.info("상품 수정 성공: productId={}, title={}", updated.getId(), updated.getTitle());
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            log.error("상품 수정 실패: productId={}, error={}", id, e.getMessage());
            throw e; // GlobalExceptionHandler에서 처리
        } catch (Exception e) {
            log.error("상품 수정 중 오류 발생: productId={}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // 상품 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        try {
        productService.deleteProduct(id);
            log.info("상품 삭제 성공: productId={}", id);
        return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("상품 삭제 중 오류 발생: productId={}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}

