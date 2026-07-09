package com.cic.inventory.repositories;

import com.cic.inventory.entities.Asset;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AssetRepositories extends JpaRepository<Asset, Long>, JpaSpecificationExecutor<Asset> {
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

    // Used by the dashboard stats endpoint — Pageable here just caps the
    // result count (e.g. top 5 soonest-expiring), no separate count query.
    //
    // NOTE: locationName/departmentName must already be lower-cased by the
    // caller (or null). Calling LOWER(:param) in JPQL when the bound value is
    // null makes PostgreSQL unable to infer the parameter's type, and it
    // defaults to bytea — "function lower(bytea) does not exist". Comparing
    // against a pre-lowered value sidesteps that entirely.
    @Query("""
    SELECT a FROM Asset a
    LEFT JOIN a.assignedTo emp
    LEFT JOIN emp.department dept
    LEFT JOIN a.location loc
    WHERE a.warrantyEnd IS NOT NULL
      AND a.warrantyEnd BETWEEN :today AND :cutoff
      AND (:locationName IS NULL OR LOWER(loc.name) = :locationName)
      AND (:departmentName IS NULL OR LOWER(dept.name) = :departmentName)
    ORDER BY a.warrantyEnd ASC
""")
    List<Asset> findWarrantyExpiring(
            @Param("locationName") String locationName,
            @Param("departmentName") String departmentName,
            @Param("today") LocalDate today,
            @Param("cutoff") LocalDate cutoff,
            Pageable pageable
    );

    // Aggregate breakdowns for the Reports page charts/KPIs — small, fast
    // queries so those can render immediately instead of waiting on a fetch
    // of every asset row.
    @Query("SELECT a.status, COUNT(a) FROM Asset a GROUP BY a.status")
    List<Object[]> countByStatusGroup();

    @Query("SELECT a.category, COUNT(a) FROM Asset a GROUP BY a.category")
    List<Object[]> countByCategoryGroup();
}
