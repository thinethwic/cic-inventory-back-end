package com.cic.inventory.services.impl;

import com.cic.inventory.exceptions.InventoryException;
import com.cic.inventory.services.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageServiceImpl implements FileStorageService {

    private static final List<String> DOCUMENT_EXTENSIONS =
            List.of("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "csv");
    private static final List<String> IMAGE_EXTENSIONS =
            List.of("jpg", "jpeg", "png", "gif", "webp");

    @Value("${file.upload-dir:/uploads}")
    private String uploadDir;

    @Override
    public String storeDocument(MultipartFile file) {
        return store(file, "documents", DOCUMENT_EXTENSIONS);
    }

    @Override
    public String storeImage(MultipartFile file) {
        return store(file, "images", IMAGE_EXTENSIONS);
    }

    private String store(MultipartFile file, String folder, List<String> allowedExtensions) {
        if (file == null || file.isEmpty()) {
            throw new InventoryException("File must not be empty", HttpStatus.BAD_REQUEST);
        }

        String originalFilename = StringUtils.cleanPath(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "file"
        );
        String extension = extractExtension(originalFilename);

        if (!allowedExtensions.contains(extension.toLowerCase(Locale.ROOT))) {
            throw new InventoryException(
                    "Unsupported file type: ." + extension,
                    HttpStatus.BAD_REQUEST
            );
        }

        try {
            Path folderPath = Paths.get(uploadDir, folder);
            Files.createDirectories(folderPath);

            String storedFileName = UUID.randomUUID() + "_" + originalFilename;
            Path targetPath = folderPath.resolve(storedFileName);

            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            return folder + "/" + storedFileName;
        } catch (IOException e) {
            log.error("Failed to store file", e);
            throw new InventoryException("Failed to store file", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Resource loadAsResource(String relativePath) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(relativePath).normalize();
            if (!filePath.startsWith(Paths.get(uploadDir).normalize())) {
                throw new InventoryException("Invalid file path", HttpStatus.BAD_REQUEST);
            }

            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new InventoryException("File not found", HttpStatus.NOT_FOUND);
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new InventoryException("File not found", HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public void delete(String relativePath) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(relativePath).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.error("Failed to delete file at {}", relativePath, e);
        }
    }

    @Override
    public boolean isImage(String relativePath) {
        return IMAGE_EXTENSIONS.contains(extractExtension(relativePath).toLowerCase(Locale.ROOT));
    }

    private String extractExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex >= 0 && dotIndex < filename.length() - 1 ? filename.substring(dotIndex + 1) : "";
    }
}
