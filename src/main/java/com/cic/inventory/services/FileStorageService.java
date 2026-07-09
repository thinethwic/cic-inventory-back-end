package com.cic.inventory.services;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String storeDocument(MultipartFile file);
    String storeImage(MultipartFile file);
    Resource loadAsResource(String relativePath);
    void delete(String relativePath);
    boolean isImage(String relativePath);
}
