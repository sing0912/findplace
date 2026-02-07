package com.petpro.domain.pet.service;

import com.petpro.global.exception.BusinessException;
import com.petpro.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

/**
 * 반려동물 이미지 서비스
 * S3 (MinIO)를 사용한 프로필 이미지 업로드/삭제 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PetImageService {

    private final S3Client s3Client;

    @Value("${app.minio.bucket}")
    private String bucketName;

    @Value("${app.minio.endpoint}")
    private String minioEndpoint;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    /**
     * 프로필 이미지 업로드
     *
     * @param petId 반려동물 ID
     * @param file 업로드할 파일
     * @return 업로드된 이미지 URL
     */
    public String uploadProfileImage(Long petId, MultipartFile file) {
        validateFile(file);

        String extension = getExtension(file.getOriginalFilename());
        String objectKey = String.format("pets/%d/profile_%s.%s", petId, UUID.randomUUID(), extension);

        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            return String.format("%s/%s/%s", minioEndpoint, bucketName, objectKey);
        } catch (IOException e) {
            log.error("Failed to upload pet profile image: petId={}, error={}", petId, e.getMessage());
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    /**
     * 프로필 이미지 삭제
     *
     * @param imageUrl 삭제할 이미지 URL
     */
    public void deleteProfileImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }

        try {
            String objectKey = extractObjectKey(imageUrl);
            if (objectKey != null) {
                DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(objectKey)
                        .build();

                s3Client.deleteObject(deleteRequest);
            }
        } catch (Exception e) {
            log.warn("Failed to delete pet profile image: url={}, error={}", imageUrl, e.getMessage());
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.INVALID_FILE_TYPE);
        }

        String extension = getExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new BusinessException(ErrorCode.INVALID_FILE_TYPE);
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    private String extractObjectKey(String imageUrl) {
        String prefix = String.format("%s/%s/", minioEndpoint, bucketName);
        if (imageUrl.startsWith(prefix)) {
            return imageUrl.substring(prefix.length());
        }
        return null;
    }
}
