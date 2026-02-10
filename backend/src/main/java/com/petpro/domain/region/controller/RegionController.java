package com.petpro.domain.region.controller;

import com.petpro.domain.region.dto.RegionResponse.*;
import com.petpro.domain.region.service.RegionCodeService;
import com.petpro.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Region", description = "지역 코드 API")
@RestController
@RequestMapping("/v1/regions")
@RequiredArgsConstructor
public class RegionController {

    private final RegionCodeService regionCodeService;

    @Operation(summary = "광역시/도 목록 조회", description = "전체 광역시/도 목록을 조회합니다.")
    @GetMapping("/metros")
    public ResponseEntity<ApiResponse<MetroListDto>> getMetros() {
        MetroListDto result = regionCodeService.getMetros();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Operation(summary = "시/군/구 목록 조회", description = "특정 광역시/도의 시/군/구 목록을 조회합니다.")
    @GetMapping("/{metroCode}/cities")
    public ResponseEntity<ApiResponse<CityListDto>> getCities(
            @Parameter(description = "광역시/도 코드") @PathVariable String metroCode) {
        CityListDto result = regionCodeService.getCities(metroCode);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Operation(summary = "지역 상세 조회", description = "지역 코드로 상세 정보를 조회합니다.")
    @GetMapping("/{code}")
    public ResponseEntity<ApiResponse<RegionDto>> getByCode(
            @Parameter(description = "지역 코드") @PathVariable String code) {
        RegionDto result = regionCodeService.getByCode(code);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Operation(summary = "계층 구조 조회", description = "전체 지역을 계층 구조로 조회합니다.")
    @GetMapping("/hierarchy")
    public ResponseEntity<ApiResponse<HierarchyListDto>> getHierarchy() {
        HierarchyListDto result = regionCodeService.getHierarchy();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Operation(summary = "전체 활성 지역 목록", description = "모든 활성화된 지역 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<RegionDto>>> getAllActive() {
        List<RegionDto> result = regionCodeService.getAllActive();
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
