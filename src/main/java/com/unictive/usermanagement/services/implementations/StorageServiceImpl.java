package com.unictive.usermanagement.services.implementations;

import com.unictive.usermanagement.configs.AppPropertiesConfig;
import com.unictive.usermanagement.exceptions.types.StorageException;
import com.unictive.usermanagement.services.interfaces.StorageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "app.storage.type", havingValue = "local", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class StorageServiceImpl implements StorageService {
    private final AppPropertiesConfig appProperties;

    @Override
    public String store(MultipartFile file, String directory) {
        try {
            String uploadDir = appProperties.getStorage().getLocal().getUploadDir();
            Path dirPath = Paths.get(uploadDir, directory);
            Files.createDirectories(dirPath);

            String originalFilename = file.getOriginalFilename();
            String extension = getExtension(originalFilename);
            String fileName = UUID.randomUUID() + "." + extension;

            Path targetPath = dirPath.resolve(fileName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            String storedPath = "/" + uploadDir + "/" + directory + "/" + fileName;
            log.info("File stored at: {}", storedPath);
            return storedPath;

        } catch (IOException e) {
            throw new StorageException("Failed to store file: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) return;

        try {
            String relativePath = fileUrl.startsWith("/") ? fileUrl.substring(1) : fileUrl;
            Path filePath = Paths.get(relativePath);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("File deleted: {}", fileUrl);
            }
        } catch (IOException e) {
            log.warn("Failed to delete file {}: {}", fileUrl, e.getMessage());
        }
    }

    @Override
    public boolean isAvailable() {
        try {
            String uploadDir = appProperties.getStorage().getLocal().getUploadDir();
            Path dirPath = Paths.get(uploadDir);
            Files.createDirectories(dirPath);
            return Files.isWritable(dirPath);
        } catch (IOException e) {
            return false;
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "bin";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
