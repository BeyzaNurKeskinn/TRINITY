package com.project.Trinity.Controller;

import com.project.Trinity.Entity.Category;
import com.project.Trinity.Entity.Status;
import com.project.Trinity.Repository.CategoryRepository;
import com.project.Trinity.Service.CategoryService;
import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@PreAuthorize("isAuthenticated()")
public class CategoryController {

    private final CategoryService categoryService;
    @Autowired
    private CategoryRepository categoryRepository;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // Admin için kategori işlemleri
    @PostMapping("/admin/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Category> createCategory(@Valid @RequestBody CategoryRequest request) {
        Category category = categoryService.createCategory(
                request.getName(),
                request.getDescription(),
                Status.valueOf(request.getStatus())
        );
        return new ResponseEntity<>(category, HttpStatus.CREATED);
    }

    @GetMapping("/admin/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @PutMapping("/admin/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Category> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        Category updatedCategory = categoryService.updateCategory(
                id,
                request.getName(),
                request.getDescription(),
                Status.valueOf(request.getStatus())
        );
        return ResponseEntity.ok(updatedCategory);
    }

    @DeleteMapping("/admin/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    // Normal kullanıcılar için kategorileri döndüren endpoint
    @GetMapping("/user/categories")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<CategoryDTO>> getUserCategories() {
        List<Category> categories = categoryRepository.findByStatus(Status.ACTIVE);
        List<CategoryDTO> categoryDTOs = categories.stream()
                .map(category -> new CategoryDTO(category.getId(), category.getName()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(categoryDTOs);
    }

    @Data
    public static class CategoryDTO {
        private Long id;
        private String name;

        public CategoryDTO(Long id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}

@Data
class CategoryRequest {
    @jakarta.validation.constraints.NotBlank(message = "Kategori adı zorunludur")
    @jakarta.validation.constraints.Size(min = 3, max = 100, message = "Kategori adı 3-100 karakter olmalı")
    private String name;

    @jakarta.validation.constraints.Size(max = 500, message = "Açıklama 500 karakterden uzun olamaz")
    private String description;

    @jakarta.validation.constraints.Pattern(
            regexp = "ACTIVE|INACTIVE",
            message = "Durum ACTIVE veya INACTIVE olmalı"
    )
    private String status = "ACTIVE";
}