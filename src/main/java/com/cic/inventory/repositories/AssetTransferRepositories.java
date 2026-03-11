package com.cic.inventory.repositories;

import com.cic.inventory.entities.AssetTransfer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssetTransferRepositories extends JpaRepository<AssetTransfer,Long> {
}
