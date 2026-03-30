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

    @Query(value = """
        SELECT m.* FROM maintenance m
        JOIN assets a ON m.asset_id = a.id
        WHERE (
            :search IS NULL OR
            LOWER(m.ticket_no::text)                       LIKE LOWER(CONCAT('%', :search, '%')) OR
            LOWER(m.issue_title::text)                     LIKE LOWER(CONCAT('%', :search, '%')) OR
            LOWER(COALESCE(m.assigned_to, '')::text)       LIKE LOWER(CONCAT('%', :search, '%')) OR
            LOWER(COALESCE(a.asset_code, '')::text)        LIKE LOWER(CONCAT('%', :search, '%'))
        )
        AND (:status   IS NULL OR m.status::text   = :status)
        AND (:priority IS NULL OR m.priority::text = :priority)
        AND (:location IS NULL OR LOWER(COALESCE(m.location, '')::text) = LOWER(:location))
        """,
            countQuery = """
        SELECT COUNT(*) FROM maintenances m
        JOIN assets a ON m.asset_id = a.id
        WHERE (
            :search IS NULL OR
            LOWER(m.ticket_no::text)                       LIKE LOWER(CONCAT('%', :search, '%')) OR
            LOWER(m.issue_title::text)                     LIKE LOWER(CONCAT('%', :search, '%')) OR
            LOWER(COALESCE(m.assigned_to, '')::text)       LIKE LOWER(CONCAT('%', :search, '%')) OR
            LOWER(COALESCE(a.asset_code, '')::text)        LIKE LOWER(CONCAT('%', :search, '%'))
        )
        AND (:status   IS NULL OR m.status::text   = :status)
        AND (:priority IS NULL OR m.priority::text = :priority)
        AND (:location IS NULL OR LOWER(COALESCE(m.location, '')::text) = LOWER(:location))
        """,
            nativeQuery = true)
    Page<Maintenance> findAllFiltered(
            @Param("search")   String search,
            @Param("status")   String status,
            @Param("priority") String priority,
            @Param("location") String location,
            Pageable pageable
    );
}
