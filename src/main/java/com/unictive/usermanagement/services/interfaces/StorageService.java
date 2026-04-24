package com.unictive.usermanagement.services.interfaces;

import org.springframework.web.multipart.MultipartFile;

/**
 * Abstraction layer for file storage.
 * Implementations can be local, S3, GCS, etc.
 * Switch implementations via configuration without touching business logic.
 */
public interface StorageService {

    /**
     * Store a file and return its accessible URL / path.
     *
     * @param file     the multipart file to store
     * @param directory sub-folder within the storage (e.g., "profile-pictures")
     * @return the public URL or relative path to retrieve the file
     */
    String store(MultipartFile file, String directory);

    /**
     * Delete a file by its URL / path.
     *
     * @param fileUrl the URL or path returned by {@link #store}
     */
    void delete(String fileUrl);

    /**
     * Check whether the storage backend is available.
     */
    boolean isAvailable();
}
