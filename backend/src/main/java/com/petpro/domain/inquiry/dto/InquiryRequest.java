package com.petpro.domain.inquiry.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 문의 요청 DTO 클래스
 */
public class InquiryRequest {

    /**
     * 문의 작성 요청 DTO
     */
    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class Create {
        /** 문의 제목 (필수, 최대 200자) */
        @NotBlank(message = "제목을 입력해주세요.")
        @Size(max = 200, message = "제목은 200자 이하여야 합니다.")
        private String title;

        /** 문의 내용 (필수) */
        @NotBlank(message = "내용을 입력해주세요.")
        private String content;
    }

    /**
     * 문의 수정 요청 DTO
     */
    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class Update {
        /** 문의 제목 (필수, 최대 200자) */
        @NotBlank(message = "제목을 입력해주세요.")
        @Size(max = 200, message = "제목은 200자 이하여야 합니다.")
        private String title;

        /** 문의 내용 (필수) */
        @NotBlank(message = "내용을 입력해주세요.")
        private String content;
    }
}
