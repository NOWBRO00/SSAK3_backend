package org.example.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.*;
import org.example.repository.CategoryRepository;
import org.example.repository.ProductImageRepository;
import org.example.repository.ProductRepository;
import org.example.repository.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserProfileRepository userProfileRepository;
    private final ProductImageRepository productImageRepository;

    private static final String UPLOAD_DIR = System.getProperty("user.dir") + File.separator + "uploads" + File.separator;

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
        // 카테고리 & 판매자 조회
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다. categoryId: " + categoryId));
        
        // sellerId가 카카오 ID일 수도 있고, UserProfile의 id일 수도 있음
        UserProfile seller = userProfileRepository.findById(sellerId)
                .orElse(null);
        
        // UserProfile의 id로 찾지 못했으면 카카오 ID로 시도
        if (seller == null) {
            log.debug("UserProfile id로 사용자를 찾지 못함. kakaoId로 시도: {}", sellerId);
            seller = userProfileRepository.findByKakaoId(sellerId);
        }
        
        if (seller == null) {
            log.error("사용자를 찾을 수 없습니다. sellerId={} (UserProfile id 또는 kakaoId로 조회 실패).", sellerId);
            log.error("카카오 로그인 API(/api/auth/kakao)를 먼저 호출하여 사용자를 등록해야 합니다.");
            throw new IllegalArgumentException("존재하지 않는 사용자입니다. sellerId: " + sellerId + " (카카오 로그인을 먼저 진행해주세요.)");
        }
        
        log.debug("판매자 조회 성공 - userId={}, kakaoId={}, nickname={}", seller.getId(), seller.getKakaoId(), seller.getNickname());

        // 상품 엔티티 생성 및 저장
        Product product = new Product();
        product.setTitle(title);
        product.setPrice(price);
        product.setDescription(description != null ? description : "");
        product.setStatus(ProductStatus.ON_SALE);
        product.setCategory(category);
        product.setSeller(seller);

        Product savedProduct = productRepository.save(product);

        // 이미지 처리
        if (images != null && !images.isEmpty()) {
            List<ProductImage> imageEntities = new ArrayList<>();

            // 업로드 디렉토리 생성
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                boolean created = uploadDir.mkdirs();
                if (!created) {
                    throw new IOException("업로드 디렉토리를 생성할 수 없습니다: " + UPLOAD_DIR);
                }
            }

            for (int i = 0; i < images.size(); i++) {
                MultipartFile file = images.get(i);
                if (file != null && !file.isEmpty()) {
                    try {
                        // 원본 파일명에서 특수문자 제거
                        String originalFilename = file.getOriginalFilename();
                        if (originalFilename == null || originalFilename.trim().isEmpty()) {
                            originalFilename = "image.jpg";
                        }
                        
                        // 파일 확장자 추출
                        String extension = "";
                        int lastDotIndex = originalFilename.lastIndexOf('.');
                        if (lastDotIndex > 0) {
                            extension = originalFilename.substring(lastDotIndex);
                        }
                        
                        // 파일명에서 특수문자 제거 및 공백을 언더스코어로 변경
                        String safeFilename = originalFilename.substring(0, lastDotIndex > 0 ? lastDotIndex : originalFilename.length())
                                .replaceAll("[^a-zA-Z0-9._-]", "_") + extension;
                        
                        // 파일명 생성
                        String uniqueName = UUID.randomUUID().toString().replace("-", "") + "_" + safeFilename;

                    // 실제 파일 저장
                    File destination = new File(UPLOAD_DIR + uniqueName);
                    file.transferTo(destination);

                        // DB에 이미지 정보 저장 (URL은 웹 경로로 저장)
                        String imageUrl = "/uploads/" + uniqueName;
                    ProductImage image = ProductImage.builder()
                            .product(savedProduct)
                                .imageUrl(imageUrl)
                            .orderIndex(i)
                            .build();

                    productImageRepository.save(image);
                    imageEntities.add(image);
                    } catch (IOException e) {
                        // 개별 이미지 저장 실패 시 로그만 남기고 계속 진행
                        System.err.println("이미지 저장 실패: " + (file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown") + " - " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }

            // Product에 이미지 세팅
            savedProduct.setImages(imageEntities);
        }

        return savedProduct;
    }

    // 전체 상품 조회
    public List<Product> getAllProducts() {
        try {
            log.info("전체 상품 조회 시작");
            List<Product> products = productRepository.findAll();
            log.info("상품 조회 완료: {}개", products != null ? products.size() : 0);
            
            // seller와 category를 명시적으로 초기화하여 LazyInitializationException 방지
            // 트랜잭션 내에서 실행되므로 안전하게 접근 가능
            if (products != null && !products.isEmpty()) {
                List<Product> validProducts = new ArrayList<>();
                products.forEach(product -> {
                    try {
                        // seller 초기화 및 검증
                        if (product.getSeller() == null) {
                            log.warn("상품 {}의 seller가 null입니다. 건너뜁니다.", product.getId());
                            return;
                        }
                        product.getSeller().getId();
                        product.getSeller().getNickname();
                        
                        // category 초기화 및 검증
                        if (product.getCategory() == null) {
                            log.warn("상품 {}의 category가 null입니다. 건너뜁니다.", product.getId());
                            return;
                        }
                        product.getCategory().getId();
                        product.getCategory().getName();
                        
                        // images 초기화
                        if (product.getImages() != null) {
                            product.getImages().size();
                        }
                        
                        validProducts.add(product);
                    } catch (Exception e) {
                        log.error("상품 {} 초기화 중 오류: {}", product.getId(), e.getMessage(), e);
                    }
                });
                log.info("유효한 상품 {}개 반환", validProducts.size());
                return validProducts;
            }
            log.info("상품이 없습니다. 빈 리스트 반환");
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("상품 조회 중 오류 발생: {}", e.getMessage(), e);
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // 상품 상세조회
    public Optional<Product> getProductById(Long id) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            // Lazy 로딩 엔티티 초기화
            try {
                if (product.getSeller() != null) {
                    product.getSeller().getId();
                    product.getSeller().getNickname();
                    product.getSeller().getKakaoId(); // sellerKakaoId를 위한 초기화
                }
                if (product.getCategory() != null) {
                    product.getCategory().getId();
                    product.getCategory().getName();
                }
                if (product.getImages() != null) {
                    product.getImages().size();
                }
            } catch (Exception e) {
                System.err.println("상품 초기화 중 오류: " + e.getMessage());
            }
        }
        return productOpt;
    }

    // 카테고리별 조회
    public List<Product> getProductsByCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다."));
        List<Product> products = productRepository.findByCategory(category);
        // Lazy 로딩 엔티티 초기화
        if (products != null && !products.isEmpty()) {
            products.forEach(product -> {
                try {
                    if (product.getSeller() != null) {
                        product.getSeller().getId();
                        product.getSeller().getNickname();
                    }
                    if (product.getCategory() != null) {
                        product.getCategory().getId();
                        product.getCategory().getName();
                    }
                    if (product.getImages() != null) {
                        product.getImages().size();
                    }
                } catch (Exception e) {
                    System.err.println("상품 초기화 중 오류: " + e.getMessage());
                }
            });
        }
        return products != null ? products : new ArrayList<>();
    }

    // 판매자별 조회
    public List<Product> getProductsBySeller(Long sellerId) {
        // sellerId가 카카오 ID일 수도 있고, UserProfile의 id일 수도 있음
        UserProfile seller = userProfileRepository.findById(sellerId)
                .orElse(null);
        
        // UserProfile의 id로 찾지 못했으면 카카오 ID로 시도
        if (seller == null) {
            seller = userProfileRepository.findByKakaoId(sellerId);
        }
        
        if (seller == null) {
            // 사용자가 없으면 빈 리스트 반환
            return new ArrayList<>();
        }
        
        List<Product> products = productRepository.findBySeller(seller);
        // seller, category, images를 명시적으로 초기화하여 LazyInitializationException 방지
        if (products != null && !products.isEmpty()) {
            products.forEach(product -> {
                try {
                    // seller 초기화
                    if (product.getSeller() != null) {
                        product.getSeller().getId();
                        product.getSeller().getNickname();
                    }
                    // category 초기화
                    if (product.getCategory() != null) {
                        product.getCategory().getId();
                        product.getCategory().getName();
                    }
                    // images 초기화
                    if (product.getImages() != null) {
                        product.getImages().size();
                    }
                } catch (Exception e) {
                    System.err.println("상품 초기화 중 오류: " + e.getMessage());
                }
            });
        }
        return products != null ? products : new ArrayList<>();
    }

    // 키워드로 상품 검색
    public List<Product> searchProducts(String keyword) {
        try {
            log.info("상품 검색 시작: keyword={}", keyword);
            
            if (keyword == null || keyword.trim().isEmpty()) {
                log.warn("검색 키워드가 비어있습니다. 전체 상품을 반환합니다.");
                return getAllProducts();
            }
            
            List<Product> products = productRepository.searchByKeyword(keyword.trim());
            log.info("상품 검색 완료: keyword={}, count={}", keyword, products != null ? products.size() : 0);
            
            // Lazy 로딩 엔티티 초기화
            if (products != null && !products.isEmpty()) {
                List<Product> validProducts = new ArrayList<>();
                products.forEach(product -> {
                    try {
                        // seller 초기화 및 검증
                        if (product.getSeller() == null) {
                            log.warn("상품 {}의 seller가 null입니다. 건너뜁니다.", product.getId());
                            return;
                        }
                        product.getSeller().getId();
                        product.getSeller().getNickname();
                        product.getSeller().getKakaoId();
                        
                        // category 초기화 및 검증
                        if (product.getCategory() == null) {
                            log.warn("상품 {}의 category가 null입니다. 건너뜁니다.", product.getId());
                            return;
                        }
                        product.getCategory().getId();
                        product.getCategory().getName();
                        
                        // images 초기화
                        if (product.getImages() != null) {
                            product.getImages().size();
                            product.getImages().forEach(img -> {
                                if (img != null) {
                                    img.getId();
                                    img.getImageUrl();
                                    img.getOrderIndex();
                                }
                            });
                        }
                        
                        validProducts.add(product);
                    } catch (Exception e) {
                        log.error("상품 {} 초기화 중 오류: {}", product != null ? product.getId() : "null", e.getMessage(), e);
                    }
                });
                log.info("유효한 상품 {}개 반환", validProducts.size());
                return validProducts;
            }
            log.info("검색 결과가 없습니다. 빈 리스트 반환");
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("상품 검색 중 오류 발생: keyword={}, error={}", keyword, e.getMessage(), e);
            e.printStackTrace();
            return new ArrayList<>();
        }
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

