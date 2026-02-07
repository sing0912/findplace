package com.petpro.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * OAuth 설정 프로퍼티
 */
@Configuration
@ConfigurationProperties(prefix = "app.oauth")
@Getter
@Setter
public class OAuthProperties {

    private ProviderConfig google = new ProviderConfig();
    private ProviderConfig kakao = new ProviderConfig();
    private ProviderConfig naver = new ProviderConfig();

    @Getter
    @Setter
    public static class ProviderConfig {
        private String clientId;
        private String clientSecret;
        private String redirectUri;
        private String tokenUri;
        private String userInfoUri;
    }
}
