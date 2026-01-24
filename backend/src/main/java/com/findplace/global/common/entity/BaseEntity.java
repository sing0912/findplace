package com.findplace.global.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * BaseEntity
 *
 * 모든 엔티티의 공통 필드를 제공하는 추상 기본 클래스입니다.
 * 생성/수정 시간과 생성자/수정자 정보를 자동으로 추적합니다.
 *
 * 포함 필드:
 * - createdAt: 엔티티 생성 일시 (자동 설정, 수정 불가)
 * - updatedAt: 엔티티 최종 수정 일시 (자동 갱신)
 * - createdBy: 엔티티 생성자 ID (자동 설정, 수정 불가)
 * - updatedBy: 엔티티 최종 수정자 ID (자동 갱신)
 *
 * 사용 방법:
 * 이 클래스를 상속받아 엔티티를 정의하면 Auditing 필드가 자동으로 추가됩니다.
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    /** 엔티티 생성 일시 (최초 저장 시 자동 설정) */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 엔티티 최종 수정 일시 (저장 시마다 자동 갱신) */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /** 엔티티 생성자 ID (최초 저장 시 자동 설정) */
    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    /** 엔티티 최종 수정자 ID (저장 시마다 자동 갱신) */
    @LastModifiedBy
    @Column(name = "updated_by")
    private Long updatedBy;
}
