
package com.project.Trinity.Controller;

import com.project.Trinity.Entity.Category;
import com.project.Trinity.Entity.Status;
import com.project.Trinity.Service.CategoryService;
import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/category")
@PreAuthorize("hasRole('ADMIN')")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<Category> createCategory(@Valid @RequestBody CategoryRequest request) {
        Category category = categoryService.createCategory(
                request.getName(),
                request.getLogo(),
                Status.valueOf(request.getStatus())
        );
        return new ResponseEntity<>(category, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        Category updatedCategory = categoryService.updateCategory(
                id,
                request.getName(),
                request.getLogo(),
                Status.valueOf(request.getStatus())
        );
        return ResponseEntity.ok(updatedCategory);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}

@Data
class CategoryRequest {
    @jakarta.validation.constraints.NotBlank(message = "Kategori adı zorunludur")
    @jakarta.validation.constraints.Size(min = 3, max = 100, message = "Kategori adı 3-100 karakter olmalı")
    private String name;

    @jakarta.validation.constraints.Pattern(
            regexp = "^$|^data:image/(png|jpeg|jpg|gif);base64,[A-Za-z0-9+/=]+$",
            message = "Geçerli bir Base64 görüntü formatı giriniz (png, jpeg, jpg, gif desteklenir)"
    )
    private String logo;

    @jakarta.validation.constraints.NotBlank(message = "Durum zorunludur")
    @jakarta.validation.constraints.Pattern(
            regexp = "ACTIVE|INACTIVE",
            message = "Durum ACTIVE veya INACTIVE olmalı"
    )
    private String status;
}