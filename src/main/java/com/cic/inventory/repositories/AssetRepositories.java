package com.cic.inventory.repositories;

import com.cic.inventory.entities.Asset;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AssetRepositories extends JpaRepository<Asset, Long> {
    Optional<Asset> findByBarcode(String barcode);

    Optional<Asset> findBySerialNo(String serialNo);

    Optional<Asset> findByAssetCode(String assetCode);

    Page<Asset> findByLocation_NameIgnoreCase(String locationName, Pageable pageable);

    Page<Asset> findByAssignedTo_Department_NameIgnoreCase(
            String departmentName,
            Pageable pageable
    );

    Page<Asset> findByLocation_NameIgnoreCaseAndAssignedTo_Department_NameIgnoreCase(
            String locationName,
            String departmentName,
            Pageable pageable
    );

    // Replace the combined query with a LEFT JOIN version
    @Query("""
    SELECT DISTINCT a FROM Asset a
    LEFT JOIN a.assignedTo emp
    LEFT JOIN emp.department dept
    LEFT JOIN a.location loc
    WHERE
        (:locationName IS NOT NULL AND LOWER(loc.name) = LOWER(:locationName))
        OR
        (:departmentName IS NOT NULL AND LOWER(dept.name) = LOWER(:departmentName))
""")
    Page<Asset> findByAccessScope(
            @Param("locationName") String locationName,
            @Param("departmentName") String departmentName,
            Pageable pageable
    );
}
