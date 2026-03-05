package com.cic.inventory.repositories;

import com.cic.inventory.entities.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierRepositories extends JpaRepository<Supplier,Long> {
}
