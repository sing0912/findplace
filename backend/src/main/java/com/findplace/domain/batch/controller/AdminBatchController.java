package com.findplace.domain.batch.controller;

import com.findplace.domain.batch.entity.BatchJobLog;
import com.findplace.domain.batch.job.BirthdayCouponJob;
import com.findplace.domain.batch.job.CouponExpiryJob;
import com.findplace.domain.batch.job.DormantUserJob;
import com.findplace.domain.batch.service.BatchLogService;
import com.findplace.global.common.response.ApiResponse;
import com.findplace.global.exception.BusinessException;
import com.findplace.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 관리자 배치 API
 */
@RestController
@RequestMapping("/v1/admin/batch")
@RequiredArgsConstructor
@Tag(name = "Admin Batch", description = "관리자 배치 관리 API")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminBatchController {

    private final BatchLogService batchLogService;
    private final BirthdayCouponJob birthdayCouponJob;
    private final CouponExpiryJob couponExpiryJob;
    private final DormantUserJob dormantUserJob;

    @GetMapping("/logs")
    @Operation(summary = "배치 로그 목록 조회", description = "배치 작업 로그 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<Page<BatchJobLog>>> getLogs(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<BatchJobLog> logs = batchLogService.getLogs(pageable);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }

    @GetMapping("/logs/{id}")
    @Operation(summary = "배치 로그 상세 조회", description = "배치 작업 로그 상세 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<BatchJobLog>> getLog(
            @Parameter(description = "로그 ID") @PathVariable Long id) {
        BatchJobLog log = batchLogService.getLog(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
        return ResponseEntity.ok(ApiResponse.success(log));
    }

    @PostMapping("/{jobName}/run")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "배치 수동 실행", description = "배치 작업을 수동으로 실행합니다.")
    public ResponseEntity<ApiResponse<String>> runJob(
            @Parameter(description = "작업명") @PathVariable String jobName) {

        switch (jobName.toLowerCase()) {
            case "birthdaycouponjob" -> {
                birthdayCouponJob.issueBirthdayCoupons();
                return ResponseEntity.ok(ApiResponse.success("BirthdayCouponJob 실행 완료"));
            }
            case "couponexpiryjob" -> {
                couponExpiryJob.expireCoupons();
                return ResponseEntity.ok(ApiResponse.success("CouponExpiryJob 실행 완료"));
            }
            case "dormantuserjob" -> {
                dormantUserJob.processDormantUsers();
                return ResponseEntity.ok(ApiResponse.success("DormantUserJob 실행 완료"));
            }
            default -> throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
        }
    }
}
