package com.petpro.e2e.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class AuthTestHelper {

    private final TestRestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final AtomicInteger counter = new AtomicInteger(0);

    public AuthTestHelper(TestRestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public AuthResult registerAndLogin(String baseUrl) {
        int seq = counter.incrementAndGet();
        String email = "e2euser" + seq + "@test.com";
        String password = "Test1234!";
        String name = "테스트유저" + seq;
        String nickname = "닉네임" + seq;
        String phone = "010-" + String.format("%04d", seq) + "-0001";

        // 1. 회원가입
        Map<String, Object> registerBody = Map.of(
                "email", email,
                "password", password,
                "name", name,
                "nickname", nickname,
                "phone", phone,
                "agreeTerms", true,
                "agreePrivacy", true,
                "agreeMarketing", false
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> registerResponse = restTemplate.exchange(
                baseUrl + "/v1/auth/register",
                HttpMethod.POST,
                new HttpEntity<>(registerBody, headers),
                String.class
        );

        if (registerResponse.getStatusCode() != HttpStatus.CREATED) {
            throw new RuntimeException("회원가입 실패: " + registerResponse.getStatusCode()
                    + " body=" + registerResponse.getBody());
        }

        // 2. 로그인
        return login(baseUrl, email, password);
    }

    public AuthResult login(String baseUrl, String email, String password) {
        Map<String, String> loginBody = Map.of(
                "email", email,
                "password", password
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> loginResponse = restTemplate.exchange(
                baseUrl + "/v1/auth/login",
                HttpMethod.POST,
                new HttpEntity<>(loginBody, headers),
                String.class
        );

        if (loginResponse.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("로그인 실패: " + loginResponse.getStatusCode()
                    + " body=" + loginResponse.getBody());
        }

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> body = objectMapper.readValue(loginResponse.getBody(), Map.class);
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) body.get("data");

            String accessToken = (String) data.get("accessToken");
            String refreshToken = (String) data.get("refreshToken");

            // accessToken에서 userId 추출을 위해 /users/me 호출
            HttpHeaders authHeaders = new HttpHeaders();
            authHeaders.setBearerAuth(accessToken);
            authHeaders.setContentType(MediaType.APPLICATION_JSON);

            ResponseEntity<String> meResponse = restTemplate.exchange(
                    baseUrl + "/v1/users/me",
                    HttpMethod.GET,
                    new HttpEntity<>(authHeaders),
                    String.class
            );

            Long userId = null;
            if (meResponse.getStatusCode() == HttpStatus.OK) {
                @SuppressWarnings("unchecked")
                Map<String, Object> meBody = objectMapper.readValue(meResponse.getBody(), Map.class);
                @SuppressWarnings("unchecked")
                Map<String, Object> meData = (Map<String, Object>) meBody.get("data");
                userId = ((Number) meData.get("id")).longValue();
            }

            return new AuthResult(accessToken, refreshToken, userId, email, password);
        } catch (Exception e) {
            throw new RuntimeException("로그인 응답 파싱 실패", e);
        }
    }

    public static class AuthResult {
        private final String accessToken;
        private final String refreshToken;
        private final Long userId;
        private final String email;
        private final String password;

        public AuthResult(String accessToken, String refreshToken, Long userId, String email, String password) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.userId = userId;
            this.email = email;
            this.password = password;
        }

        public String getAccessToken() { return accessToken; }
        public String getRefreshToken() { return refreshToken; }
        public Long getUserId() { return userId; }
        public String getEmail() { return email; }
        public String getPassword() { return password; }
    }
}
