package com.unictive.usermanagement.services.implementations;

import com.unictive.usermanagement.configs.AppPropertiesConfig;
import com.unictive.usermanagement.exceptions.types.InvalidFileException;
import com.unictive.usermanagement.services.interfaces.ImageProcessingService;
import com.unictive.usermanagement.utils.CompressedMultipartFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageProcessingServiceImpl implements ImageProcessingService {

    private static final String PROFILE_PICTURES_DIR = "profile-pictures";

    private final AppPropertiesConfig appProperties;

    @Override
    public MultipartFile validateAndCompress(MultipartFile file) {
        validateFile(file);
        return compress(file);
    }

    @Override
    public String getProfilePicturesDir() {
        return PROFILE_PICTURES_DIR;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("File must not be empty");
        }

        String contentType = file.getContentType();
        AppPropertiesConfig.ImageProperties imgProps = appProperties.getImage();

        if (contentType == null || !imgProps.getAllowedTypes().contains(contentType)) {
            throw new InvalidFileException(
                    "Invalid file type. Allowed types: " + imgProps.getAllowedTypes()
            );
        }

        if (file.getSize() > imgProps.getMaxSizeBytes()) {
            long maxMb = imgProps.getMaxSizeBytes() / 1024 / 1024;
            throw new InvalidFileException(
                    "File size exceeds the maximum allowed limit of " + maxMb + "MB"
            );
        }
    }

    private MultipartFile compress(MultipartFile file) {
        try {
            AppPropertiesConfig.ImageProperties imgProps = appProperties.getImage();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            Thumbnails.of(file.getInputStream())
                    .size(imgProps.getMaxWidth(), imgProps.getMaxHeight())
                    .keepAspectRatio(true)
                    .outputQuality(imgProps.getCompressionQuality())
                    .toOutputStream(outputStream);

            byte[] compressedBytes = outputStream.toByteArray();

            log.debug("Image compressed: original={}B -> compressed={}B",
                    file.getSize(), compressedBytes.length);

            return new CompressedMultipartFile(
                    file.getName(),
                    file.getOriginalFilename(),
                    file.getContentType(),
                    compressedBytes
            );

        } catch (IOException e) {
            throw new InvalidFileException("Failed to compress image: " + e.getMessage());
        }
    }
}
