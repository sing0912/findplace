package com.petpro.domain.inquiry.controller;

import com.petpro.domain.inquiry.dto.InquiryRequest;
import com.petpro.domain.inquiry.dto.InquiryResponse;
import com.petpro.domain.inquiry.service.InquiryService;
import com.petpro.global.common.response.ApiResponse;
import com.petpro.global.common.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * 문의 컨트롤러
 *
 * 문의 게시판 관련 REST API 엔드포인트
 */
@Tag(name = "Inquiries", description = "문의 게시판 API")
@RestController
@RequestMapping("/v1/inquiries")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;

    /**
     * 내 문의 목록 조회 API
     */
    @Operation(summary = "내 문의 목록", description = "로그인한 사용자의 문의 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<InquiryResponse.ListItem>>> getMyInquiries(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Long userId = Long.parseLong(userDetails.getUsername());
        Page<InquiryResponse.ListItem> page = inquiryService.getMyInquiries(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(page)));
    }

    /**
     * 문의 작성 API
     */
    @Operation(summary = "문의 작성", description = "새로운 문의를 작성합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<InquiryResponse.Detail>> createInquiry(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody InquiryRequest.Create request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        InquiryResponse.Detail response = inquiryService.createInquiry(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /**
     * 문의 상세 조회 API
     */
    @Operation(summary = "문의 상세", description = "문의 상세 내용을 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InquiryResponse.Detail>> getInquiry(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        Long userId = Long.parseLong(userDetails.getUsername());
        InquiryResponse.Detail response = inquiryService.getInquiry(userId, id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 문의 수정 API
     */
    @Operation(summary = "문의 수정", description = "문의 내용을 수정합니다. 답변 완료된 문의는 수정 불가.")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<InquiryResponse.Detail>> updateInquiry(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody InquiryRequest.Update request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        InquiryResponse.Detail response = inquiryService.updateInquiry(userId, id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 문의 삭제 API
     */
    @Operation(summary = "문의 삭제", description = "문의를 삭제합니다. 답변 완료된 문의는 삭제 불가.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInquiry(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        Long userId = Long.parseLong(userDetails.getUsername());
        inquiryService.deleteInquiry(userId, id);
        return ResponseEntity.noContent().build();
    }
}
