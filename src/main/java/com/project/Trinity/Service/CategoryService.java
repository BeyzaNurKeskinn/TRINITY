package com.project.Trinity.Service;

import com.project.Trinity.Entity.Category;
import com.project.Trinity.Entity.User;
import com.project.Trinity.Repository.CategoryRepository;
import com.project.Trinity.Entity.Status;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public Category createCategory(String name, String description, Status status) {
        Optional<Category> existingCategory = categoryRepository.findByName(name);
        if (existingCategory.isPresent()) {
            throw new IllegalArgumentException("Bu kategori adı zaten mevcut: " + name);
        }

      

        User admin = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Category category = new Category();
        category.setName(name);
        category.setDescription(description); // description eklendi

        category.setStatus(status);

        return categoryRepository.save(category);
    }

    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Category> getActiveCategories() {
        return categoryRepository.findByStatus(Status.ACTIVE);
    }

    @Transactional
    public Category updateCategory(Long id, String name, String description, Status status) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Kategori bulunamadı: " + id));

        Optional<Category> existingCategory = categoryRepository.findByName(name);
        if (existingCategory.isPresent() && !existingCategory.get().getId().equals(id)) {
            throw new IllegalArgumentException("Bu kategori adı zaten mevcut: " + name);
        }

     
        category.setName(name);
        category.setDescription(description); // description eklendi

        category.setStatus(status);

        return categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Kategori bulunamadı: " + id));
        category.setStatus(Status.INACTIVE); // Silme yerine pasifleştirme
        categoryRepository.save(category);
    }
}