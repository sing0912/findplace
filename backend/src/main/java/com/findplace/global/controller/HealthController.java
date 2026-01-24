package com.findplace.global.controller;

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
 */
@Tag(name = "Health", description = "Health Check API")
@RestController
public class HealthController {

    /**
     * 서버 상태를 확인합니다.
     * 서버가 정상 동작 중이면 "UP" 상태와 현재 타임스탬프를 반환합니다.
     *
     * @return 서버 상태 정보 (status: "UP", timestamp: 현재 시간)
     */
    @Operation(summary = "Health Check", description = "서버 상태를 확인합니다.")
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "timestamp", LocalDateTime.now().toString()
        ));
    }
}
