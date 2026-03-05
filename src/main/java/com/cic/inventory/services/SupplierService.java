package com.cic.inventory.services;

import com.cic.inventory.entities.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SupplierService {
    Supplier createNewSupplier(Supplier  supplier);
    Page<Supplier> getAllSuppliers(Pageable pageable);
    Supplier getSupplierById(Long id);
    Supplier updateSupplierById(Long id, Supplier updatedSupplier);
    void deleteSupplier(Long id);
}
