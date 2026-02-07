-- 배치 실행 로그 테이블
CREATE TABLE batch_job_execution_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_name VARCHAR(100) NOT NULL COMMENT '작업명',
    job_type VARCHAR(50) NOT NULL COMMENT '작업 유형',
    started_at DATETIME(3) NOT NULL COMMENT '시작 시간',
    completed_at DATETIME(3) COMMENT '완료 시간',
    status VARCHAR(20) NOT NULL COMMENT '상태',
    total_count INT NOT NULL DEFAULT 0 COMMENT '총 처리 건수',
    success_count INT NOT NULL DEFAULT 0 COMMENT '성공 건수',
    fail_count INT NOT NULL DEFAULT 0 COMMENT '실패 건수',
    error_message TEXT COMMENT '에러 메시지',
    execution_time_ms BIGINT COMMENT '실행 시간(ms)',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '생성일시',

    INDEX idx_batch_job_exec_job_name (job_name),
    INDEX idx_batch_job_exec_started_at (started_at),
    INDEX idx_batch_job_exec_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='배치 실행 로그';
