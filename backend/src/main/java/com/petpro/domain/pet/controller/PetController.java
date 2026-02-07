package com.petpro.domain.pet.controller;

import com.petpro.domain.pet.dto.PetChecklistRequest;
import com.petpro.domain.pet.dto.PetChecklistResponse;
import com.petpro.domain.pet.dto.PetRequest;
import com.petpro.domain.pet.dto.PetResponse;
import com.petpro.domain.pet.service.PetChecklistService;
import com.petpro.domain.pet.service.PetService;
import com.petpro.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Pet", description = "반려동물 API")
@RestController
@RequestMapping("/v1/pets")
@RequiredArgsConstructor
public class PetController {

    private final PetService petService;
    private final PetChecklistService petChecklistService;

    @Operation(summary = "내 반려동물 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<PetResponse.ListDto>> getMyPets(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = extractUserId(userDetails);
        PetResponse.ListDto result = petService.getMyPets(userId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Operation(summary = "반려동물 상세 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PetResponse.Detail>> getPet(
            @Parameter(description = "반려동물 ID") @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = extractUserId(userDetails);
        PetResponse.Detail result = petService.getPet(id, userId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Operation(summary = "반려동물 등록")
    @PostMapping
    public ResponseEntity<ApiResponse<PetResponse.Detail>> createPet(
            @Valid @RequestBody PetRequest.Create request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = extractUserId(userDetails);
        PetResponse.Detail result = petService.createPet(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(result));
    }

    @Operation(summary = "반려동물 정보 수정")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PetResponse.Detail>> updatePet(
            @Parameter(description = "반려동물 ID") @PathVariable Long id,
            @Valid @RequestBody PetRequest.Update request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = extractUserId(userDetails);
        PetResponse.Detail result = petService.updatePet(id, userId, request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Operation(summary = "프로필 이미지 업로드")
    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<PetResponse.Detail>> uploadProfileImage(
            @Parameter(description = "반려동물 ID") @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = extractUserId(userDetails);
        PetResponse.Detail result = petService.uploadProfileImage(id, userId, file);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Operation(summary = "반려동물 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePet(
            @Parameter(description = "반려동물 ID") @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = extractUserId(userDetails);
        petService.deletePet(id, userId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "사망 처리")
    @PatchMapping("/{id}/deceased")
    public ResponseEntity<ApiResponse<PetResponse.Detail>> markAsDeceased(
            @Parameter(description = "반려동물 ID") @PathVariable Long id,
            @Valid @RequestBody PetRequest.Deceased request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = extractUserId(userDetails);
        PetResponse.Detail result = petService.markAsDeceased(id, userId, request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Operation(summary = "성향 체크리스트 조회")
    @GetMapping("/{id}/checklist")
    public ResponseEntity<ApiResponse<PetChecklistResponse>> getChecklist(
            @Parameter(description = "반려동물 ID") @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = extractUserId(userDetails);
        PetChecklistResponse result = petChecklistService.getChecklist(id, userId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Operation(summary = "성향 체크리스트 생성")
    @PostMapping("/{id}/checklist")
    public ResponseEntity<ApiResponse<PetChecklistResponse>> createChecklist(
            @Parameter(description = "반려동물 ID") @PathVariable Long id,
            @Valid @RequestBody PetChecklistRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = extractUserId(userDetails);
        PetChecklistResponse result = petChecklistService.createChecklist(id, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(result));
    }

    @Operation(summary = "성향 체크리스트 수정")
    @PutMapping("/{id}/checklist")
    public ResponseEntity<ApiResponse<PetChecklistResponse>> updateChecklist(
            @Parameter(description = "반려동물 ID") @PathVariable Long id,
            @Valid @RequestBody PetChecklistRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = extractUserId(userDetails);
        PetChecklistResponse result = petChecklistService.updateChecklist(id, userId, request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    private Long extractUserId(UserDetails userDetails) {
        // UserDetails에서 userId 추출 (구현에 따라 다를 수 있음)
        return Long.parseLong(userDetails.getUsername());
    }
}
