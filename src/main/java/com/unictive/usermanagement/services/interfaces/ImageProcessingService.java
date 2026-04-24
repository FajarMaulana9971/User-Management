package com.unictive.usermanagement.services.interfaces;

import org.springframework.web.multipart.MultipartFile;

public interface ImageProcessingService {
    MultipartFile validateAndCompress(MultipartFile file);

    String getProfilePicturesDir();
}
