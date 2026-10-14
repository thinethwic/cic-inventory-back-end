package com.cic.inventory.services;

import com.cic.inventory.entities.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoryService {
    Category createNewCategory(Category  category);
    Page<Category> getAllCategories(Pageable pageable);
    Category getCategoryById(Long id);
    Category updateCategoryById(Long id, Category updatedCategory);
    void deleteCategory(Long id);
}
