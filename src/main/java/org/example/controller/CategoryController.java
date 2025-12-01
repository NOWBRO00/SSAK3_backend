package org.example.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.Category;
import org.example.repository.CategoryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;

    // 전체 카테고리 조회
    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        try {
            List<Category> categories = categoryRepository.findAll();
            log.info("카테고리 조회 성공: {}개", categories != null ? categories.size() : 0);
            return ResponseEntity.ok(categories != null ? categories : new ArrayList<>());
        } catch (Exception e) {
            log.error("카테고리 조회 중 오류 발생", e);
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    // 특정 카테고리 조회
    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        try {
            return categoryRepository.findById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("카테고리 조회 중 오류 발생: categoryId={}", id, e);
            return ResponseEntity.notFound().build();
        }
    }

    // 카테고리 이름으로 조회
    @GetMapping("/name/{name}")
    public ResponseEntity<Category> getCategoryByName(@PathVariable String name) {
        try {
            Category category = categoryRepository.findByName(name);
            if (category != null) {
                return ResponseEntity.ok(category);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("카테고리 조회 중 오류 발생: categoryName={}", name, e);
            return ResponseEntity.notFound().build();
        }
    }

    // 카테고리 생성
    @PostMapping
    public ResponseEntity<Category> createCategory(@RequestBody Category category) {
        try {
            // 입력 검증
            if (category.getName() == null || category.getName().trim().isEmpty()) {
                log.error("카테고리 생성 실패: 카테고리 이름이 비어있습니다.");
                return ResponseEntity.badRequest().build();
            }

            // 중복 확인
            Category existingCategory = categoryRepository.findByName(category.getName());
            if (existingCategory != null) {
                log.error("카테고리 생성 실패: 이미 존재하는 카테고리입니다. name={}", category.getName());
                return ResponseEntity.badRequest().build();
            }

            Category saved = categoryRepository.save(category);
            log.info("카테고리 생성 성공: categoryId={}, name={}", saved.getId(), saved.getName());
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            log.error("카테고리 생성 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // 카테고리 수정
    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        try {
            Category existingCategory = categoryRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다."));

            // 이름 변경 시 중복 확인
            if (category.getName() != null && !category.getName().equals(existingCategory.getName())) {
                Category duplicateCategory = categoryRepository.findByName(category.getName());
                if (duplicateCategory != null && !duplicateCategory.getId().equals(id)) {
                    log.error("카테고리 수정 실패: 이미 존재하는 카테고리 이름입니다. name={}", category.getName());
                    return ResponseEntity.badRequest().build();
                }
            }

            existingCategory.setName(category.getName());
            Category updated = categoryRepository.save(existingCategory);
            log.info("카테고리 수정 성공: categoryId={}, name={}", updated.getId(), updated.getName());
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            log.error("카테고리 수정 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("카테고리 수정 중 오류 발생: categoryId={}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // 카테고리 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        try {
            if (!categoryRepository.existsById(id)) {
                log.error("카테고리 삭제 실패: 카테고리를 찾을 수 없습니다. categoryId={}", id);
                return ResponseEntity.notFound().build();
            }

            categoryRepository.deleteById(id);
            log.info("카테고리 삭제 성공: categoryId={}", id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("카테고리 삭제 중 오류 발생: categoryId={}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}



