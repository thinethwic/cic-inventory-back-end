package com.cic.inventory.repositories;

import com.cic.inventory.entities.Asset;
import com.cic.inventory.entities.AssetAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssetAttachmentRepository extends JpaRepository<AssetAttachment, Long> {
    List<AssetAttachment> findByAssetOrderByUploadedAtAsc(Asset asset);

    Optional<AssetAttachment> findByIdAndAsset(Long id, Asset asset);
}
