package com.petpro.domain.inquiry.dto;

import com.petpro.domain.inquiry.entity.Inquiry;
import com.petpro.domain.inquiry.entity.InquiryAnswer;
import com.petpro.domain.inquiry.entity.InquiryStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 문의 응답 DTO 클래스
 */
public class InquiryResponse {

    /**
     * 문의 목록 아이템 응답 DTO
     */
    @Getter
    @Builder
    public static class ListItem {
        private Long id;
        private String title;
        private InquiryStatus status;
        private LocalDateTime createdAt;

        public static ListItem from(Inquiry inquiry) {
            return ListItem.builder()
                    .id(inquiry.getId())
                    .title(inquiry.getTitle())
                    .status(inquiry.getStatus())
                    .createdAt(inquiry.getCreatedAt())
                    .build();
        }
    }

    /**
     * 문의 상세 응답 DTO
     */
    @Getter
    @Builder
    public static class Detail {
        private Long id;
        private String title;
        private String content;
        private InquiryStatus status;
        private LocalDateTime createdAt;
        private AnswerInfo answer;

        public static Detail from(Inquiry inquiry) {
            return Detail.builder()
                    .id(inquiry.getId())
                    .title(inquiry.getTitle())
                    .content(inquiry.getContent())
                    .status(inquiry.getStatus())
                    .createdAt(inquiry.getCreatedAt())
                    .answer(inquiry.getAnswer() != null ? AnswerInfo.from(inquiry.getAnswer()) : null)
                    .build();
        }
    }

    /**
     * 답변 정보 응답 DTO
     */
    @Getter
    @Builder
    public static class AnswerInfo {
        private String content;
        private LocalDateTime createdAt;

        public static AnswerInfo from(InquiryAnswer answer) {
            return AnswerInfo.builder()
                    .content(answer.getContent())
                    .createdAt(answer.getCreatedAt())
                    .build();
        }
    }

    /**
     * 성공 응답 DTO
     */
    @Getter
    @Builder
    public static class Success {
        private boolean success;

        public static Success of() {
            return Success.builder()
                    .success(true)
                    .build();
        }
    }
}
