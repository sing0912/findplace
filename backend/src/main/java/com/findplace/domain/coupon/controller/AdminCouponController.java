package com.findplace.domain.coupon.controller;

import com.findplace.domain.coupon.dto.CouponRequest;
import com.findplace.domain.coupon.dto.CouponResponse;
import com.findplace.domain.coupon.service.CouponService;
import com.findplace.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 쿠폰 관리자 컨트롤러
 */
@Tag(name = "Admin - Coupon", description = "쿠폰 관리 API")
@RestController
@RequestMapping("/v1/admin/coupons")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class AdminCouponController {

    private final CouponService couponService;

    @Operation(summary = "쿠폰 유형 목록 조회")
    @GetMapping("/types")
    public ResponseEntity<ApiResponse<List<CouponResponse.TypeItem>>> getCouponTypes() {
        List<CouponResponse.TypeItem> types = couponService.getAllCouponTypes();
        return ResponseEntity.ok(ApiResponse.success(types));
    }

    @Operation(summary = "쿠폰 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<CouponResponse.ListItem>>> getCoupons(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<CouponResponse.ListItem> coupons = couponService.getCoupons(keyword, isActive, page, size);
        return ResponseEntity.ok(ApiResponse.success(coupons));
    }

    @Operation(summary = "쿠폰 상세 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CouponResponse.Detail>> getCoupon(
            @PathVariable Long id) {
        CouponResponse.Detail coupon = couponService.getCouponById(id);
        return ResponseEntity.ok(ApiResponse.success(coupon));
    }

    @Operation(summary = "쿠폰 생성")
    @PostMapping
    public ResponseEntity<ApiResponse<CouponResponse.Detail>> createCoupon(
            @Valid @RequestBody CouponRequest.Create request) {
        CouponResponse.Detail coupon = couponService.createCoupon(request);
        return ResponseEntity.ok(ApiResponse.success(coupon));
    }

    @Operation(summary = "특정 회원에게 쿠폰 발급")
    @PostMapping("/{id}/issue")
    public ResponseEntity<ApiResponse<CouponResponse.MyCoupon>> issueCoupon(
            @PathVariable Long id,
            @Valid @RequestBody CouponRequest.Issue request) {
        CouponResponse.MyCoupon coupon = couponService.issueCoupon(id, request.getUserId());
        return ResponseEntity.ok(ApiResponse.success(coupon));
    }

    @Operation(summary = "만료 쿠폰 처리 (배치)")
    @PostMapping("/expire")
    public ResponseEntity<ApiResponse<Integer>> expireCoupons() {
        int count = couponService.expireCoupons();
        return ResponseEntity.ok(ApiResponse.success(count));
    }
}
