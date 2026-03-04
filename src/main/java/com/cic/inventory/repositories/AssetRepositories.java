package com.cic.inventory.repositories;

import com.cic.inventory.entities.Asset;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssetRepositories extends JpaRepository<Asset, Long> {
}
