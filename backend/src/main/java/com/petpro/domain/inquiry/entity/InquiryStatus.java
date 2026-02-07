package com.petpro.domain.inquiry.entity;

/**
 * 문의 상태
 *
 * - WAITING: 답변 대기
 * - ANSWERED: 답변 완료
 */
public enum InquiryStatus {
    WAITING("답변 대기"),
    ANSWERED("답변 완료");

    private final String description;

    InquiryStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
