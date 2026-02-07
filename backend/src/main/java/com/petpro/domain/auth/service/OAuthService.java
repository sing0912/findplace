package com.petpro.domain.auth.service;

import com.petpro.domain.auth.entity.AuthProvider;
import com.petpro.global.config.OAuthProperties;
import com.petpro.global.exception.BusinessException;
import com.petpro.global.exception.ErrorCode;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * OAuth 소셜 로그인 서비스
 * Google, Kakao, Naver OAuth API 연동
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthService {

    private final OAuthProperties oAuthProperties;
    private final RestTemplate restTemplate;

    /**
     * OAuth 사용자 정보 조회
     *
     * @param provider OAuth 제공자
     * @param code     Authorization code
     * @return OAuth 사용자 정보
     */
    public OAuthUserInfo getOAuthUserInfo(AuthProvider provider, String code) {
        return switch (provider) {
            case GOOGLE -> getGoogleUserInfo(code);
            case KAKAO -> getKakaoUserInfo(code);
            case NAVER -> getNaverUserInfo(code);
            default -> throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "지원하지 않는 OAuth 제공자입니다.");
        };
    }

    /**
     * Google OAuth 사용자 정보 조회
     */
    private OAuthUserInfo getGoogleUserInfo(String code) {
        OAuthProperties.ProviderConfig config = oAuthProperties.getGoogle();

        // 1. Access Token 요청
        String accessToken = getGoogleAccessToken(code, config);

        // 2. 사용자 정보 요청
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    config.getUserInfoUri(),
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            Map<String, Object> userInfo = response.getBody();
            if (userInfo == null) {
                throw new BusinessException(ErrorCode.OAUTH_AUTHENTICATION_FAILED, "Google 사용자 정보를 가져올 수 없습니다.");
            }

            String providerId = (String) userInfo.get("sub");
            String email = (String) userInfo.get("email");
            String name = (String) userInfo.get("name");
            String picture = (String) userInfo.get("picture");

            if (providerId == null) {
                log.error("[Google OAuth] providerId(sub)가 null입니다. userInfo={}", userInfo);
                throw new BusinessException(ErrorCode.OAUTH_AUTHENTICATION_FAILED, "Google 사용자 식별 정보를 가져올 수 없습니다.");
            }

            log.info("[Google OAuth] 사용자 정보 조회 성공: email={}", email);

            return OAuthUserInfo.builder()
                    .provider(AuthProvider.GOOGLE)
                    .providerId(providerId)
                    .email(email)
                    .name(name)
                    .profileImageUrl(picture)
                    .build();

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("[Google OAuth] 사용자 정보 조회 실패: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.OAUTH_AUTHENTICATION_FAILED, "Google 인증에 실패했습니다.");
        }
    }

    private String getGoogleAccessToken(String code, OAuthProperties.ProviderConfig config) {
        if (config.getTokenUri() == null || config.getClientId() == null || config.getClientSecret() == null) {
            log.error("[Google OAuth] OAuth 설정이 불완전합니다. tokenUri={}, clientId={}", config.getTokenUri(), config.getClientId() != null ? "설정됨" : "null");
            throw new BusinessException(ErrorCode.OAUTH_AUTHENTICATION_FAILED, "Google OAuth 설정이 올바르지 않습니다.");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", config.getClientId());
        params.add("client_secret", config.getClientSecret());
        params.add("redirect_uri", config.getRedirectUri());
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    config.getTokenUri(),
                    entity,
                    Map.class
            );

            Map<String, Object> tokenResponse = response.getBody();
            if (tokenResponse == null || !tokenResponse.containsKey("access_token")) {
                log.error("[Google OAuth] 토큰 응답에 access_token 없음: {}", tokenResponse);
                throw new BusinessException(ErrorCode.OAUTH_AUTHENTICATION_FAILED, "Google 토큰을 가져올 수 없습니다.");
            }

            return (String) tokenResponse.get("access_token");

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("[Google OAuth] 토큰 요청 실패: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.OAUTH_AUTHENTICATION_FAILED, "Google 인증에 실패했습니다.");
        }
    }

    /**
     * Kakao OAuth 사용자 정보 조회
     */
    private OAuthUserInfo getKakaoUserInfo(String code) {
        OAuthProperties.ProviderConfig config = oAuthProperties.getKakao();

        // 1. Access Token 요청
        String accessToken = getKakaoAccessToken(code, config);

        // 2. 사용자 정보 요청
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    config.getUserInfoUri(),
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            Map<String, Object> userInfo = response.getBody();
            if (userInfo == null) {
                throw new BusinessException(ErrorCode.OAUTH_AUTHENTICATION_FAILED, "Kakao 사용자 정보를 가져올 수 없습니다.");
            }

            String providerId = String.valueOf(userInfo.get("id"));

            @SuppressWarnings("unchecked")
            Map<String, Object> kakaoAccount = (Map<String, Object>) userInfo.get("kakao_account");
            String email = kakaoAccount != null ? (String) kakaoAccount.get("email") : null;

            @SuppressWarnings("unchecked")
            Map<String, Object> profile = kakaoAccount != null ? (Map<String, Object>) kakaoAccount.get("profile") : null;
            String nickname = profile != null ? (String) profile.get("nickname") : null;
            String profileImage = profile != null ? (String) profile.get("profile_image_url") : null;

            log.info("[Kakao OAuth] 사용자 정보 조회 성공: email={}", email);

            return OAuthUserInfo.builder()
                    .provider(AuthProvider.KAKAO)
                    .providerId(providerId)
                    .email(email)
                    .name(nickname)
                    .profileImageUrl(profileImage)
                    .build();

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("[Kakao OAuth] 사용자 정보 조회 실패: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.OAUTH_AUTHENTICATION_FAILED, "Kakao 인증에 실패했습니다.");
        }
    }

    private String getKakaoAccessToken(String code, OAuthProperties.ProviderConfig config) {
        if (config.getTokenUri() == null || config.getClientId() == null) {
            log.error("[Kakao OAuth] OAuth 설정이 불완전합니다.");
            throw new BusinessException(ErrorCode.OAUTH_AUTHENTICATION_FAILED, "Kakao OAuth 설정이 올바르지 않습니다.");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", config.getClientId());
        params.add("client_secret", config.getClientSecret());
        params.add("redirect_uri", config.getRedirectUri());
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    config.getTokenUri(),
                    entity,
                    Map.class
            );

            Map<String, Object> tokenResponse = response.getBody();
            if (tokenResponse == null || !tokenResponse.containsKey("access_token")) {
                throw new BusinessException(ErrorCode.OAUTH_AUTHENTICATION_FAILED, "Kakao 토큰을 가져올 수 없습니다.");
            }

            return (String) tokenResponse.get("access_token");

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("[Kakao OAuth] 토큰 요청 실패: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.OAUTH_AUTHENTICATION_FAILED, "Kakao 인증에 실패했습니다.");
        }
    }

    /**
     * Naver OAuth 사용자 정보 조회
     */
    private OAuthUserInfo getNaverUserInfo(String code) {
        OAuthProperties.ProviderConfig config = oAuthProperties.getNaver();

        // 1. Access Token 요청
        String accessToken = getNaverAccessToken(code, config);

        // 2. 사용자 정보 요청
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    config.getUserInfoUri(),
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            Map<String, Object> body = response.getBody();
            if (body == null) {
                throw new BusinessException(ErrorCode.OAUTH_AUTHENTICATION_FAILED, "Naver 사용자 정보를 가져올 수 없습니다.");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> userInfo = (Map<String, Object>) body.get("response");
            if (userInfo == null) {
                throw new BusinessException(ErrorCode.OAUTH_AUTHENTICATION_FAILED, "Naver 사용자 정보를 가져올 수 없습니다.");
            }

            String providerId = (String) userInfo.get("id");
            String email = (String) userInfo.get("email");
            String name = (String) userInfo.get("name");
            String profileImage = (String) userInfo.get("profile_image");

            log.info("[Naver OAuth] 사용자 정보 조회 성공: email={}", email);

            return OAuthUserInfo.builder()
                    .provider(AuthProvider.NAVER)
                    .providerId(providerId)
                    .email(email)
                    .name(name)
                    .profileImageUrl(profileImage)
                    .build();

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("[Naver OAuth] 사용자 정보 조회 실패: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.OAUTH_AUTHENTICATION_FAILED, "Naver 인증에 실패했습니다.");
        }
    }

    private String getNaverAccessToken(String code, OAuthProperties.ProviderConfig config) {
        if (config.getTokenUri() == null || config.getClientId() == null) {
            log.error("[Naver OAuth] OAuth 설정이 불완전합니다.");
            throw new BusinessException(ErrorCode.OAUTH_AUTHENTICATION_FAILED, "Naver OAuth 설정이 올바르지 않습니다.");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", config.getClientId());
        params.add("client_secret", config.getClientSecret());
        params.add("redirect_uri", config.getRedirectUri());
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    config.getTokenUri(),
                    entity,
                    Map.class
            );

            Map<String, Object> tokenResponse = response.getBody();
            if (tokenResponse == null || !tokenResponse.containsKey("access_token")) {
                throw new BusinessException(ErrorCode.OAUTH_AUTHENTICATION_FAILED, "Naver 토큰을 가져올 수 없습니다.");
            }

            return (String) tokenResponse.get("access_token");

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("[Naver OAuth] 토큰 요청 실패: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.OAUTH_AUTHENTICATION_FAILED, "Naver 인증에 실패했습니다.");
        }
    }

    /**
     * OAuth 사용자 정보 DTO
     */
    @Getter
    @Builder
    public static class OAuthUserInfo {
        private final AuthProvider provider;
        private final String providerId;
        private final String email;
        private final String name;
        private final String profileImageUrl;
    }
}
