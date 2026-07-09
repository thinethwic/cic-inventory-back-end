package com.cic.inventory.services.impl;

import com.cic.inventory.dtos.responses.AssetAttachmentDTO;
import com.cic.inventory.entities.Asset;
import com.cic.inventory.entities.AssetAttachment;
import com.cic.inventory.exceptions.InventoryException;
import com.cic.inventory.repositories.AssetAttachmentRepository;
import com.cic.inventory.repositories.AssetRepositories;
import com.cic.inventory.services.AssetAttachmentService;
import com.cic.inventory.services.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssetAttachmentServiceImpl implements AssetAttachmentService {

    private static final List<String> IMAGE_EXTENSIONS = List.of("jpg", "jpeg", "png", "gif", "webp");

    private final AssetAttachmentRepository assetAttachmentRepository;
    private final AssetRepositories assetRepositories;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional
    public AssetAttachmentDTO upload(Long assetId, MultipartFile file) {
        Asset asset = getAsset(assetId);

        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
        String extension = extractExtension(originalFilename).toLowerCase(Locale.ROOT);
        String relativePath = IMAGE_EXTENSIONS.contains(extension)
                ? fileStorageService.storeImage(file)
                : fileStorageService.storeDocument(file);

        AssetAttachment attachment = new AssetAttachment();
        attachment.setAsset(asset);
        attachment.setFileName(originalFilename);
        attachment.setFilePath(relativePath);
        attachment.setFileSize(file.getSize());
        attachment.setContentType(file.getContentType());

        AssetAttachment saved = assetAttachmentRepository.save(attachment);
        return toDto(saved);
    }

    @Override
    public List<AssetAttachmentDTO> list(Long assetId) {
        Asset asset = getAsset(assetId);
        return assetAttachmentRepository.findByAssetOrderByUploadedAtAsc(asset)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public AssetAttachment getForAccess(Long assetId, Long attachmentId) {
        Asset asset = getAsset(assetId);
        return assetAttachmentRepository.findByIdAndAsset(attachmentId, asset)
                .orElseThrow(() -> new InventoryException("Attachment not found", HttpStatus.NOT_FOUND));
    }

    @Override
    @Transactional
    public void delete(Long assetId, Long attachmentId) {
        AssetAttachment attachment = getForAccess(assetId, attachmentId);
        fileStorageService.delete(attachment.getFilePath());
        assetAttachmentRepository.delete(attachment);
    }

    private Asset getAsset(Long assetId) {
        return assetRepositories.findById(assetId)
                .orElseThrow(() -> new InventoryException("Asset not found", HttpStatus.NOT_FOUND));
    }

    private AssetAttachmentDTO toDto(AssetAttachment attachment) {
        return new AssetAttachmentDTO(
                attachment.getId(),
                attachment.getFileName(),
                attachment.getFileSize(),
                attachment.getContentType(),
                attachment.getUploadedAt()
        );
    }

    private String extractExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex >= 0 && dotIndex < filename.length() - 1 ? filename.substring(dotIndex + 1) : "";
    }
}
