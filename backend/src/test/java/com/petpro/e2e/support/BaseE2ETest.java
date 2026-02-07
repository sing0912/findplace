package com.petpro.e2e.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petpro.e2e.config.E2ETestConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.MultiValueMap;

import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(resolver = E2EProfileResolver.class)
@Import(E2ETestConfig.class)
@org.junit.jupiter.api.TestInstance(org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseE2ETest {

    @LocalServerPort
    protected int port;

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected AuthTestHelper authTestHelper;

    @Autowired
    protected AdminTestHelper adminTestHelper;

    protected String baseUrl() {
        return "http://localhost:" + port + "/api";
    }

    // ========== HTTP 헬퍼 메서드 ==========

    protected ResponseEntity<String> getWithAuth(String path, String token) {
        return restTemplate.exchange(
                baseUrl() + path,
                HttpMethod.GET,
                new HttpEntity<>(authHeaders(token)),
                String.class
        );
    }

    protected ResponseEntity<String> getPublic(String path) {
        return restTemplate.exchange(
                baseUrl() + path,
                HttpMethod.GET,
                new HttpEntity<>(publicHeaders()),
                String.class
        );
    }

    protected ResponseEntity<String> postWithAuth(String path, Object body, String token) {
        return restTemplate.exchange(
                baseUrl() + path,
                HttpMethod.POST,
                new HttpEntity<>(body, authHeaders(token)),
                String.class
        );
    }

    protected ResponseEntity<String> postPublic(String path, Object body) {
        return restTemplate.exchange(
                baseUrl() + path,
                HttpMethod.POST,
                new HttpEntity<>(body, publicHeaders()),
                String.class
        );
    }

    protected ResponseEntity<String> putWithAuth(String path, Object body, String token) {
        return restTemplate.exchange(
                baseUrl() + path,
                HttpMethod.PUT,
                new HttpEntity<>(body, authHeaders(token)),
                String.class
        );
    }

    protected ResponseEntity<String> deleteWithAuth(String path, String token) {
        return restTemplate.exchange(
                baseUrl() + path,
                HttpMethod.DELETE,
                new HttpEntity<>(authHeaders(token)),
                String.class
        );
    }

    protected ResponseEntity<String> deleteWithAuthAndBody(String path, Object body, String token) {
        return restTemplate.exchange(
                baseUrl() + path,
                HttpMethod.DELETE,
                new HttpEntity<>(body, authHeaders(token)),
                String.class
        );
    }

    protected ResponseEntity<String> patchWithAuth(String path, String token) {
        return restTemplate.exchange(
                baseUrl() + path,
                HttpMethod.PATCH,
                new HttpEntity<>(authHeaders(token)),
                String.class
        );
    }

    protected ResponseEntity<String> patchWithAuth(String path, Object body, String token) {
        return restTemplate.exchange(
                baseUrl() + path,
                HttpMethod.PATCH,
                new HttpEntity<>(body, authHeaders(token)),
                String.class
        );
    }

    protected ResponseEntity<String> postMultipartWithAuth(String path, MultiValueMap<String, Object> body, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        return restTemplate.exchange(
                baseUrl() + path,
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                String.class
        );
    }

    // ========== 헤더 헬퍼 ==========

    protected HttpHeaders authHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    protected HttpHeaders publicHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    // ========== JSON 파싱 헬퍼 ==========

    @SuppressWarnings("unchecked")
    protected String extractField(String responseBody, String dotPath) {
        try {
            Map<String, Object> map = objectMapper.readValue(responseBody, Map.class);
            String[] parts = dotPath.split("\\.");
            Object current = map;
            for (String part : parts) {
                if (current instanceof Map) {
                    current = ((Map<String, Object>) current).get(part);
                } else {
                    return null;
                }
            }
            return current != null ? current.toString() : null;
        } catch (Exception e) {
            throw new RuntimeException("JSON 파싱 실패: " + dotPath, e);
        }
    }

    @SuppressWarnings("unchecked")
    protected Object extractObject(String responseBody, String dotPath) {
        try {
            Map<String, Object> map = objectMapper.readValue(responseBody, Map.class);
            String[] parts = dotPath.split("\\.");
            Object current = map;
            for (String part : parts) {
                if (current instanceof Map) {
                    current = ((Map<String, Object>) current).get(part);
                } else {
                    return null;
                }
            }
            return current;
        } catch (Exception e) {
            throw new RuntimeException("JSON 파싱 실패: " + dotPath, e);
        }
    }
}
