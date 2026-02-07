package com.petpro.domain.pet.service;

import com.petpro.global.exception.BusinessException;
import com.petpro.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("PetImageService 테스트")
class PetImageServiceTest {

    @InjectMocks
    private PetImageService petImageService;

    @Mock
    private S3Client s3Client;

    @Nested
    @DisplayName("이미지 업로드")
    class UploadProfileImage {

        @Test
        @DisplayName("성공: 프로필 이미지 업로드")
        void shouldUploadImage() {
            // given
            ReflectionTestUtils.setField(petImageService, "bucketName", "petpro");
            ReflectionTestUtils.setField(petImageService, "minioEndpoint", "http://localhost:9000");

            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.jpg", "image/jpeg", new byte[1024]);

            given(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                    .willReturn(PutObjectResponse.builder().build());

            // when
            String result = petImageService.uploadProfileImage(1L, file);

            // then
            assertThat(result).startsWith("http://localhost:9000/petpro/pets/1/profile_");
            assertThat(result).endsWith(".jpg");
            verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        }

        @Test
        @DisplayName("실패: 빈 파일")
        void shouldThrowWhenFileIsEmpty() {
            // given
            MockMultipartFile emptyFile = new MockMultipartFile(
                    "file", "test.jpg", "image/jpeg", new byte[0]);

            // when & then
            assertThatThrownBy(() -> petImageService.uploadProfileImage(1L, emptyFile))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT);
        }

        @Test
        @DisplayName("실패: 파일 크기 초과 (5MB 초과)")
        void shouldThrowWhenFileTooLarge() {
            // given
            byte[] largeContent = new byte[6 * 1024 * 1024]; // 6MB
            MockMultipartFile largeFile = new MockMultipartFile(
                    "file", "test.jpg", "image/jpeg", largeContent);

            // when & then
            assertThatThrownBy(() -> petImageService.uploadProfileImage(1L, largeFile))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_FILE_TYPE);
        }

        @Test
        @DisplayName("실패: 허용되지 않은 파일 확장자")
        void shouldThrowWhenInvalidExtension() {
            // given
            MockMultipartFile invalidFile = new MockMultipartFile(
                    "file", "test.bmp", "image/bmp", new byte[1024]);

            // when & then
            assertThatThrownBy(() -> petImageService.uploadProfileImage(1L, invalidFile))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_FILE_TYPE);
        }
    }

    @Nested
    @DisplayName("이미지 삭제")
    class DeleteProfileImage {

        @Test
        @DisplayName("성공: 프로필 이미지 삭제")
        void shouldDeleteImage() {
            // given
            ReflectionTestUtils.setField(petImageService, "bucketName", "petpro");
            ReflectionTestUtils.setField(petImageService, "minioEndpoint", "http://localhost:9000");

            String imageUrl = "http://localhost:9000/petpro/pets/1/profile_abc.jpg";

            // when
            petImageService.deleteProfileImage(imageUrl);

            // then
            verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
        }

        @Test
        @DisplayName("성공: null URL인 경우 무시")
        void shouldIgnoreNullUrl() {
            // when
            petImageService.deleteProfileImage(null);

            // then
            verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
        }
    }
}
