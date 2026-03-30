package com.cic.inventory.repositories;

import com.cic.inventory.entities.Maintenance;
import com.cic.inventory.entities.types.MaintenanceStatus;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MaintenanceRepositories extends JpaRepository<Maintenance,Long> {
    Optional<Maintenance> findTopByOrderByIdDesc();

    // True if asset has any ticket NOT in the closed statuses list
    boolean existsByAsset_IdAndStatusNotIn(
            Long assetId,
            List<MaintenanceStatus> closedStatuses
    );

    // Same but excludes a specific ticket id (used on UPDATE to exclude self)
    boolean existsByAsset_IdAndStatusNotInAndIdNot(
            Long assetId,
            List<MaintenanceStatus> closedStatuses,
            Long excludeId
    );

    Page<Maintenance> findByLocationContainingIgnoreCase(String location, Pageable pageable);

    @Query("""
    SELECT m FROM Maintenance m
    JOIN m.asset a
    WHERE (
        :search IS NULL OR
        LOWER(m.ticketNo)   LIKE LOWER(CONCAT('%', :search, '%')) OR
        LOWER(m.issueTitle) LIKE LOWER(CONCAT('%', :search, '%')) OR
        LOWER(COALESCE(m.assignedTo, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
        LOWER(COALESCE(a.assetCode, '')) LIKE LOWER(CONCAT('%', :search, '%'))
    )
    AND (:status   IS NULL OR CAST(m.status   AS string) = :status)
    AND (:priority IS NULL OR CAST(m.priority AS string) = :priority)
    AND (:location IS NULL OR LOWER(COALESCE(m.location, '')) = LOWER(:location))
    """)
    Page<Maintenance> findAllFiltered(
            @Param("search")   String search,
            @Param("status")   String status,
            @Param("priority") String priority,
            @Param("location") String location,
            Pageable pageable
    );
}
