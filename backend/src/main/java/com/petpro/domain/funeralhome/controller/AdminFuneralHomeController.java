package com.petpro.domain.funeralhome.controller;

import com.petpro.domain.funeralhome.dto.FuneralHomeRequest;
import com.petpro.domain.funeralhome.dto.FuneralHomeResponse;
import com.petpro.domain.funeralhome.service.FuneralHomeService;
import com.petpro.domain.funeralhome.service.FuneralHomeSyncService;
import com.petpro.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 장례식장 관리자 컨트롤러
 */
@Tag(name = "Admin - Funeral Home", description = "장례식장 관리 API")
@RestController
@RequestMapping("/v1/admin/funeral-homes")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class AdminFuneralHomeController {

    private final FuneralHomeService funeralHomeService;
    private final FuneralHomeSyncService syncService;

    @Operation(summary = "장례식장 전체 목록 조회 (관리자)",
            description = "활성/비활성 포함 전체 장례식장 목록을 조회합니다")
    @GetMapping
    public ResponseEntity<ApiResponse<?>> findAll(
            @Parameter(description = "검색 키워드")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "지역 코드")
            @RequestParam(required = false) String locCode,
            @Parameter(description = "화장장 필터")
            @RequestParam(required = false) Boolean hasCrematorium,
            @Parameter(description = "장례식장 필터")
            @RequestParam(required = false) Boolean hasFuneral,
            @Parameter(description = "납골당 필터")
            @RequestParam(required = false) Boolean hasColumbarium,
            @Parameter(description = "활성화 상태")
            @RequestParam(required = false) Boolean isActive,
            @Parameter(description = "페이지 번호")
            @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "페이지 크기")
            @RequestParam(defaultValue = "20") Integer size) {

        FuneralHomeRequest.ListSearch request = FuneralHomeRequest.ListSearch.builder()
                .keyword(keyword)
                .locCode(locCode)
                .hasCrematorium(hasCrematorium)
                .hasFuneral(hasFuneral)
                .hasColumbarium(hasColumbarium)
                .isActive(isActive) // 관리자는 모든 상태 조회 가능
                .page(page)
                .size(size)
                .build();

        var result = funeralHomeService.findBySearchConditions(request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Operation(summary = "장례식장 상태 변경", description = "장례식장의 활성화/비활성화 상태를 변경합니다")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Void>> updateStatus(
            @Parameter(description = "장례식장 ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody FuneralHomeRequest.StatusUpdate request) {

        funeralHomeService.updateStatus(id, request.getIsActive());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "증분 동기화 실행", description = "공공 API에서 증분 데이터를 동기화합니다")
    @PostMapping("/sync/incremental")
    public ResponseEntity<ApiResponse<FuneralHomeResponse.SyncResult>> runIncrementalSync() {
        FuneralHomeResponse.SyncResult result = syncService.runIncrementalSync();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Operation(summary = "전체 동기화 실행", description = "공공 API에서 전체 데이터를 동기화합니다")
    @PostMapping("/sync/full")
    public ResponseEntity<ApiResponse<FuneralHomeResponse.SyncResult>> runFullSync() {
        FuneralHomeResponse.SyncResult result = syncService.runFullSync();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Operation(summary = "동기화 로그 조회", description = "동기화 실행 로그를 조회합니다")
    @GetMapping("/sync/logs")
    public ResponseEntity<ApiResponse<?>> getSyncLogs(
            @Parameter(description = "페이지 번호")
            @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "페이지 크기")
            @RequestParam(defaultValue = "20") Integer size) {

        var result = funeralHomeService.getSyncLogs(page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Operation(summary = "통계 조회", description = "장례식장 통계 정보를 조회합니다")
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<FuneralHomeService.FuneralHomeStats>> getStats() {
        var stats = funeralHomeService.getStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @Operation(summary = "캐시 삭제", description = "장례식장 캐시를 삭제합니다")
    @PostMapping("/cache/evict")
    public ResponseEntity<ApiResponse<Void>> evictCache() {
        funeralHomeService.evictCache();
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
