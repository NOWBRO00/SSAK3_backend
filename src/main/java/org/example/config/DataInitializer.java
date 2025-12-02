package org.example.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.Category;
import org.example.repository.CategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * 애플리케이션 시작 시 초기 데이터를 생성하는 클래스
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public void run(String... args) {
        initializeCategories();
    }

    /**
     * 기본 카테고리를 초기화합니다.
     * 카테고리가 없을 경우에만 생성합니다.
     */
    private void initializeCategories() {
        log.info("카테고리 초기화 시작");

        // 기본 카테고리 목록 (프론트엔드에서 사용하는 영어 이름 포함)
        List<String> defaultCategories = Arrays.asList(
            "의류",      // clothes
            "전자제품",  // electronics
            "가구",      // furniture
            "도서",      // books
            "스포츠",    // sports
            "뷰티",      // beauty
            "식품",      // food
            "기타",      // other
            "clothes",   // 프론트엔드 호환성
            "electronics",
            "furniture",
            "books",
            "sports",
            "beauty",
            "food",
            "other"
        );

        int createdCount = 0;
        for (String categoryName : defaultCategories) {
            Category existingCategory = categoryRepository.findByName(categoryName);
            if (existingCategory == null) {
                Category category = Category.builder()
                        .name(categoryName)
                        .build();
                categoryRepository.save(category);
                createdCount++;
                log.info("카테고리 생성: {}", categoryName);
            }
        }

        log.info("카테고리 초기화 완료: {}개 생성됨 (총 {}개)", 
                createdCount, categoryRepository.count());
    }
}

