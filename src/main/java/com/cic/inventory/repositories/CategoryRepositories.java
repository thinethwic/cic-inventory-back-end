package com.cic.inventory.repositories;

import com.cic.inventory.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepositories extends JpaRepository<Category, Long> {
}
