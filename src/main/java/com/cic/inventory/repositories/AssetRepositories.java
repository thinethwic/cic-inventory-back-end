package com.cic.inventory.repositories;

import com.cic.inventory.entities.Asset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AssetRepositories extends JpaRepository<Asset, Long> {
    Optional<Asset> findByBarcode(String barcode);

    Optional<Asset> findBySerialNo(String serialNo);

    Optional<Asset> findByAssetCode(String assetCode);
}
