package com.petpro.global.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * BaseTimeEntity
 *
 * 시간 정보만 포함하는 기본 엔티티 추상 클래스입니다.
 * 생성자/수정자 추적이 필요 없는 엔티티에 사용합니다.
 *
 * 포함 필드:
 * - createdAt: 엔티티 생성 일시 (자동 설정, 수정 불가)
 * - updatedAt: 엔티티 최종 수정 일시 (자동 갱신)
 *
 * BaseEntity와의 차이점:
 * - BaseEntity: 시간 + 사용자 추적 (createdBy, updatedBy 포함)
 * - BaseTimeEntity: 시간만 추적 (사용자 정보 없음)
 *
 * 사용 예시:
 * - 시스템에서 자동 생성되는 데이터
 * - 사용자 추적이 불필요한 메타데이터
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseTimeEntity {

    /** 엔티티 생성 일시 (최초 저장 시 자동 설정) */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 엔티티 최종 수정 일시 (저장 시마다 자동 갱신) */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
