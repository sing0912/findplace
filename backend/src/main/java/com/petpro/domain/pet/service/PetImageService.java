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
import java.io.InputStream;
import java.util.Map;
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

    @Value("${app.minio.public-url}")
    private String minioPublicUrl;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    // 파일 매직바이트로 실제 이미지 여부 검증
    private static final Map<String, byte[]> MAGIC_BYTES = Map.of(
            "jpg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF},
            "png", new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47},
            "gif", new byte[]{0x47, 0x49, 0x46, 0x38},
            "webp", new byte[]{0x52, 0x49, 0x46, 0x46}
    );

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

            return String.format("%s/%s/%s", minioPublicUrl, bucketName, objectKey);
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

        // 매직바이트 검증: 확장자를 위조해도 실제 파일 내용 확인
        validateMagicBytes(file, extension.toLowerCase());
    }

    private void validateMagicBytes(MultipartFile file, String extension) {
        String magicKey = "jpeg".equals(extension) ? "jpg" : extension;
        byte[] expected = MAGIC_BYTES.get(magicKey);
        if (expected == null) {
            return;
        }
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[expected.length];
            if (is.read(header) < expected.length) {
                throw new BusinessException(ErrorCode.INVALID_FILE_TYPE);
            }
            for (int i = 0; i < expected.length; i++) {
                if (header[i] != expected[i]) {
                    throw new BusinessException(ErrorCode.INVALID_FILE_TYPE);
                }
            }
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INVALID_FILE_TYPE);
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        // Path Traversal 방지: 파일명에서 경로 구분자 제거 후 확장자만 추출
        String safeName = filename.replace("\\", "/");
        safeName = safeName.substring(safeName.lastIndexOf("/") + 1);
        return safeName.substring(safeName.lastIndexOf(".") + 1);
    }

    private String extractObjectKey(String imageUrl) {
        String prefix = String.format("%s/%s/", minioPublicUrl, bucketName);
        if (imageUrl.startsWith(prefix)) {
            return imageUrl.substring(prefix.length());
        }
        return null;
    }
}
