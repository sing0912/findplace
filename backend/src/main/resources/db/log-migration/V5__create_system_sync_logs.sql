-- 시스템 연동 로그 테이블
CREATE TABLE system_sync_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sync_type VARCHAR(50) NOT NULL COMMENT '동기화 유형',
    source_system VARCHAR(50) NOT NULL COMMENT '소스 시스템',
    started_at DATETIME(3) NOT NULL COMMENT '시작 시간',
    completed_at DATETIME(3) COMMENT '완료 시간',
    status VARCHAR(20) NOT NULL COMMENT '상태',
    total_count INT NOT NULL DEFAULT 0 COMMENT '총 처리 건수',
    inserted_count INT NOT NULL DEFAULT 0 COMMENT '추가 건수',
    updated_count INT NOT NULL DEFAULT 0 COMMENT '수정 건수',
    deleted_count INT NOT NULL DEFAULT 0 COMMENT '삭제 건수',
    error_count INT NOT NULL DEFAULT 0 COMMENT '에러 건수',
    error_message TEXT COMMENT '에러 메시지',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '생성일시',

    INDEX idx_system_sync_logs_sync_type (sync_type),
    INDEX idx_system_sync_logs_source (source_system),
    INDEX idx_system_sync_logs_started_at (started_at),
    INDEX idx_system_sync_logs_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='시스템 연동 로그';
