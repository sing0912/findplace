package com.petpro.domain.coupon.controller;

import com.petpro.domain.coupon.dto.CouponRequest;
import com.petpro.domain.coupon.dto.CouponResponse;
import com.petpro.domain.coupon.service.CouponService;
import com.petpro.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 쿠폰 컨트롤러 (사용자)
 */
@Tag(name = "Coupon", description = "쿠폰 API")
@RestController
@RequestMapping("/v1/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @Operation(summary = "내 쿠폰 목록 조회", description = "로그인한 사용자의 쿠폰 목록을 조회합니다")
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<CouponResponse.MyCoupon>>> getMyCoupons(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        List<CouponResponse.MyCoupon> coupons = couponService.getMyCoupons(userId);
        return ResponseEntity.ok(ApiResponse.success(coupons));
    }

    @Operation(summary = "사용 가능한 쿠폰 조회", description = "사용 가능한 쿠폰 목록을 조회합니다")
    @GetMapping("/my/available")
    public ResponseEntity<ApiResponse<List<CouponResponse.MyCoupon>>> getMyAvailableCoupons(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        List<CouponResponse.MyCoupon> coupons = couponService.getMyAvailableCoupons(userId);
        return ResponseEntity.ok(ApiResponse.success(coupons));
    }

    @Operation(summary = "쿠폰 코드 등록", description = "쿠폰 코드를 입력하여 쿠폰을 등록합니다")
    @PostMapping("/redeem")
    public ResponseEntity<ApiResponse<CouponResponse.MyCoupon>> registerCoupon(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CouponRequest.RegisterCode request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        CouponResponse.MyCoupon coupon = couponService.registerCouponCode(request.getCode(), userId);
        return ResponseEntity.ok(ApiResponse.success(coupon));
    }
}
