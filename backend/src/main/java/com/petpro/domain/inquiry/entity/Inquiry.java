package com.petpro.domain.inquiry.entity;

import com.petpro.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 문의 엔티티
 *
 * 사용자가 관리자에게 문의하는 게시글
 * - 답변이 완료되면 수정/삭제 불가
 */
@Entity
@Table(name = "inquiries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 문의 작성자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 문의 제목 */
    @Column(nullable = false, length = 200)
    private String title;

    /** 문의 내용 */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    /** 문의 상태 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private InquiryStatus status = InquiryStatus.WAITING;

    /** 답변 */
    @OneToOne(mappedBy = "inquiry", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private InquiryAnswer answer;

    /** 생성 시간 */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 수정 시간 */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 문의 수정
     */
    public void update(String title, String content) {
        if (this.status == InquiryStatus.ANSWERED) {
            throw new IllegalStateException("답변이 완료된 문의는 수정할 수 없습니다.");
        }
        this.title = title;
        this.content = content;
    }

    /**
     * 답변 완료 처리
     */
    public void markAsAnswered() {
        this.status = InquiryStatus.ANSWERED;
    }

    /**
     * 수정/삭제 가능 여부 확인
     */
    public boolean isModifiable() {
        return this.status == InquiryStatus.WAITING;
    }

    /**
     * 본인 문의인지 확인
     */
    public boolean isOwner(Long userId) {
        return this.user.getId().equals(userId);
    }
}
