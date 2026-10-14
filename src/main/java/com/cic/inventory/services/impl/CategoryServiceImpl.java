package com.cic.inventory.services.impl;

import com.cic.inventory.entities.Category;
import com.cic.inventory.exceptions.InventoryException;
import com.cic.inventory.repositories.CategoryRepositories;
import com.cic.inventory.services.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final ModelMapper modelMapper;
    private final CategoryRepositories categoryRepositories;
    @Override
    public Category createNewCategory(Category category) {
        try {
            return categoryRepositories.save(category);
        } catch (Exception exception) {
            log.error("Failed to create new category", exception);
            throw new InventoryException("Failed to create new category", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Page<Category> getAllCategories(Pageable pageable) {
        try {
            return categoryRepositories.findAll(pageable);
        } catch (Exception exception) {
            log.error("Failed to get all categories", exception);
            throw new InventoryException("Failed to get all categories", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Category getCategoryById(Long id) {
        try {
            Category category =  categoryRepositories.findById(id).orElseThrow(
                    () -> new InventoryException("Category Not Found", HttpStatus.NOT_FOUND)
            );
            return category;
        }catch (InventoryException inventoryException){
            log.warn("category not found with id: {} to fetch", id, inventoryException);
            throw new InventoryException("category Not found", HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public Category updateCategoryById(Long id, Category updatedCategory) {
        try {
            Category category =  categoryRepositories.findById(id).orElseThrow(
                    () -> new InventoryException("Category Not Found", HttpStatus.NOT_FOUND)
            );
            modelMapper.map(updatedCategory, category);
            return categoryRepositories.save(category);
        }catch (InventoryException inventoryException){
            log.warn("Location not found with id: {} to fetch", id, inventoryException);
            throw new InventoryException("Location Not found", HttpStatus.NOT_FOUND);

        } catch (Exception exception) {
            log.error("Error updating location", exception);
            throw new InventoryException("Failed to update location", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void deleteCategory(Long id) {
        try {
            categoryRepositories.deleteById(id);
        } catch (Exception exception) {
            log.error("Failed to delete category with id {}", id, exception);
            throw new InventoryException("Failed to delete category", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
