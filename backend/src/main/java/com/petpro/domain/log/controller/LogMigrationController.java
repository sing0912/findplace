package com.petpro.domain.log.controller;

import com.petpro.domain.log.migration.LogDataMigrationService;
import com.petpro.domain.log.migration.LogDataMigrationService.MigrationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 로그 데이터 이관 API 컨트롤러
 *
 * 기존 PostgreSQL 로그 데이터를 MySQL로 이관하는 관리자 전용 엔드포인트
 */
@RestController
@RequestMapping("/v1/admin/log-migration")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class LogMigrationController {

    private final LogDataMigrationService migrationService;

    /**
     * 로그 데이터 이관 실행
     */
    @PostMapping("/execute")
    public ResponseEntity<MigrationResult> executeMigration() {
        MigrationResult result = migrationService.executeFullMigration();
        return ResponseEntity.ok(result);
    }
}
