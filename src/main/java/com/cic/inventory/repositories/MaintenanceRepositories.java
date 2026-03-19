package com.cic.inventory.repositories;

import com.cic.inventory.entities.Employee;
import com.cic.inventory.entities.Maintenance;
import com.cic.inventory.entities.types.MaintenanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
