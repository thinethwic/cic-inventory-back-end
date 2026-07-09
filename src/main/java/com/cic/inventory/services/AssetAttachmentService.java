package com.cic.inventory.services;

import com.cic.inventory.dtos.responses.AssetAttachmentDTO;
import com.cic.inventory.entities.AssetAttachment;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public interface AssetAttachmentService {
    AssetAttachmentDTO upload(Long assetId, MultipartFile file);
    List<AssetAttachmentDTO> list(Long assetId);
    AssetAttachment getForAccess(Long assetId, Long attachmentId);
    void delete(Long assetId, Long attachmentId);
}
