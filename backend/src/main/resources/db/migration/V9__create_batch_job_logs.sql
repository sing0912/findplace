-- 배치 작업 로그 테이블
CREATE TABLE batch_job_logs (
    id BIGSERIAL PRIMARY KEY,
    job_name VARCHAR(100) NOT NULL,
    job_type VARCHAR(50) NOT NULL,
    started_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    status VARCHAR(20) NOT NULL,
    total_count INTEGER DEFAULT 0,
    success_count INTEGER DEFAULT 0,
    fail_count INTEGER DEFAULT 0,
    error_message TEXT,
    execution_time_ms BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 인덱스
CREATE INDEX idx_batch_job_logs_job_name ON batch_job_logs(job_name);
CREATE INDEX idx_batch_job_logs_started_at ON batch_job_logs(started_at DESC);
CREATE INDEX idx_batch_job_logs_status ON batch_job_logs(status);

-- 코멘트
COMMENT ON TABLE batch_job_logs IS '배치 작업 로그';
COMMENT ON COLUMN batch_job_logs.job_name IS '작업명';
COMMENT ON COLUMN batch_job_logs.job_type IS '작업 유형';
COMMENT ON COLUMN batch_job_logs.status IS '상태 (RUNNING, COMPLETED, FAILED)';
