package com.petpro.global.controller;

import com.petpro.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * HealthController
 *
 * 서버 상태 확인을 위한 Health Check 엔드포인트를 제공하는 컨트롤러입니다.
 * 로드 밸런서, 모니터링 시스템, Kubernetes 등에서 서버 상태를 확인할 때 사용됩니다.
 *
 * 제공 엔드포인트:
 * - GET /health: 서버 상태 및 현재 시간 반환
 *
 * 참고: /health 경로는 nginx 헬스체크 등 인프라에서 참조하므로 /v1/ 접두사를 적용하지 않음
 */
@Tag(name = "Health", description = "Health Check API")
@RestController
public class HealthController {

    @Operation(summary = "Health Check", description = "서버 상태를 확인합니다.")
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        Map<String, Object> data = Map.of(
                "status", "UP",
                "timestamp", LocalDateTime.now().toString()
        );
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
