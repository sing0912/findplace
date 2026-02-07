package com.petpro.domain.funeralhome.controller;

import com.petpro.domain.funeralhome.dto.FuneralHomeRequest;
import com.petpro.domain.funeralhome.dto.FuneralHomeResponse;
import com.petpro.domain.funeralhome.service.FuneralHomeService;
import com.petpro.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 장례식장 컨트롤러 (사용자)
 */
@Tag(name = "Funeral Home", description = "장례식장 API")
@RestController
@RequestMapping("/v1/funeral-homes")
@RequiredArgsConstructor
public class FuneralHomeController {

    private final FuneralHomeService funeralHomeService;

    @Operation(summary = "근처 장례식장 검색", description = "현재 위치 기준으로 근처 장례식장을 검색합니다")
    @GetMapping("/nearby")
    public ResponseEntity<ApiResponse<FuneralHomeResponse.NearbyResult>> findNearby(
            @Parameter(description = "위도", required = true, example = "37.5065")
            @RequestParam Double latitude,
            @Parameter(description = "경도", required = true, example = "127.0536")
            @RequestParam Double longitude,
            @Parameter(description = "검색 반경 (km)", example = "10")
            @RequestParam(defaultValue = "10") Integer radius,
            @Parameter(description = "결과 수", example = "20")
            @RequestParam(defaultValue = "20") Integer limit,
            @Parameter(description = "화장장 필터")
            @RequestParam(required = false) Boolean hasCrematorium,
            @Parameter(description = "장례식장 필터")
            @RequestParam(required = false) Boolean hasFuneral,
            @Parameter(description = "납골당 필터")
            @RequestParam(required = false) Boolean hasColumbarium) {

        FuneralHomeRequest.NearbySearch request = FuneralHomeRequest.NearbySearch.builder()
                .latitude(latitude)
                .longitude(longitude)
                .radius(radius)
                .limit(limit)
                .hasCrematorium(hasCrematorium)
                .hasFuneral(hasFuneral)
                .hasColumbarium(hasColumbarium)
                .build();

        FuneralHomeResponse.NearbyResult result = funeralHomeService.findNearby(request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Operation(summary = "장례식장 상세 조회", description = "장례식장 상세 정보를 조회합니다")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FuneralHomeResponse.Detail>> findById(
            @Parameter(description = "장례식장 ID", required = true)
            @PathVariable Long id) {

        FuneralHomeResponse.Detail detail = funeralHomeService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(detail));
    }

    @Operation(summary = "장례식장 목록 조회", description = "조건에 맞는 장례식장 목록을 조회합니다")
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
                .isActive(true) // 사용자 API는 활성화된 항목만
                .page(page)
                .size(size)
                .build();

        var result = funeralHomeService.findBySearchConditions(request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
