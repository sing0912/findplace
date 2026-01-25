package com.findplace.domain.location.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Google Maps API 설정
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "location.google-maps")
public class GoogleMapsConfig {

    /** Google Maps API 키 */
    private String apiKey;

    /** 기본 언어 */
    private String language = "ko";

    /** 기본 지역 */
    private String region = "KR";

    /** API 요청 타임아웃 (ms) */
    private int timeout = 5000;

    /** 최대 재시도 횟수 */
    private int maxRetries = 3;

    /** API 키가 설정되어 있는지 확인 */
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }
}
