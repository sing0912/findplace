package com.petpro.domain.home.controller;

import com.petpro.domain.home.dto.HomeResponse;
import com.petpro.domain.home.service.HomeService;
import com.petpro.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Home", description = "홈 화면 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/v1/home")
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    @Operation(summary = "반려인 홈 조회", description = "반려인(CUSTOMER) 홈 화면 데이터를 조회합니다.")
    @GetMapping("/customer")
    public ResponseEntity<ApiResponse<HomeResponse.CustomerHome>> getCustomerHome(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "현재 위치 위도") @RequestParam(required = false) Double latitude,
            @Parameter(description = "현재 위치 경도") @RequestParam(required = false) Double longitude) {
        Long userId = extractUserId(userDetails);
        HomeResponse.CustomerHome response = homeService.getCustomerHome(userId, latitude, longitude);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "펫시터 홈 조회", description = "펫시터(PARTNER) 홈 화면 데이터를 조회합니다.")
    @GetMapping("/partner")
    public ResponseEntity<ApiResponse<HomeResponse.PartnerHome>> getPartnerHome(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = extractUserId(userDetails);
        HomeResponse.PartnerHome response = homeService.getPartnerHome(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    private Long extractUserId(UserDetails userDetails) {
        return Long.parseLong(userDetails.getUsername());
    }
}
