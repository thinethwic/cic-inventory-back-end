package com.cic.inventory.configs;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@Slf4j
public class StorageConfig {

    @Value("${file.upload-dir:/uploads}")
    private String uploadDir;

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadDir, "images"));
            Files.createDirectories(Paths.get(uploadDir, "documents"));
        } catch (IOException e) {
            log.error("Failed to create upload directories under {}", uploadDir, e);
        }
    }
}
