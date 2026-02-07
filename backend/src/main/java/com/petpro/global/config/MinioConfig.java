package com.petpro.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

/**
 * MinioConfig
 *
 * MinIO 오브젝트 스토리지 연결을 위한 S3 클라이언트 설정 클래스입니다.
 * MinIO는 S3 호환 API를 제공하므로 AWS SDK를 사용하여 연결합니다.
 *
 * 주요 기능:
 * - S3Client: 파일 업로드/다운로드/삭제 등의 기본 작업
 * - S3Presigner: 임시 서명된 URL 생성 (파일 직접 접근용)
 */
@Configuration
public class MinioConfig {

    /** MinIO 서버 엔드포인트 URL */
    @Value("${app.minio.endpoint}")
    private String endpoint;

    /** MinIO 접근 키 (사용자 ID) */
    @Value("${app.minio.access-key}")
    private String accessKey;

    /** MinIO 비밀 키 (비밀번호) */
    @Value("${app.minio.secret-key}")
    private String secretKey;

    /**
     * S3 클라이언트를 생성합니다.
     * MinIO 서버와의 파일 작업(업로드, 다운로드, 삭제 등)에 사용됩니다.
     * forcePathStyle(true)은 MinIO의 경로 스타일 접근을 위해 필수입니다.
     *
     * @return S3Client 인스턴스
     */
    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .forcePathStyle(true)
                .build();
    }

    /**
     * S3 Presigner를 생성합니다.
     * 미리 서명된 URL을 생성하여 클라이언트가 직접 파일에 접근할 수 있도록 합니다.
     * 주로 파일 다운로드 URL 제공 시 사용됩니다.
     *
     * @return S3Presigner 인스턴스
     */
    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
    }
}
