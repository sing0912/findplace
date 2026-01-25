-- 사용자 상태 변경 로그 테이블
CREATE TABLE user_status_change_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    previous_status VARCHAR(20) NOT NULL,
    new_status VARCHAR(20) NOT NULL,
    reason TEXT,
    changed_by BIGINT REFERENCES users(id),
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 사용자 역할 변경 로그 테이블
CREATE TABLE user_role_change_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    previous_role VARCHAR(20) NOT NULL,
    new_role VARCHAR(20) NOT NULL,
    reason TEXT,
    changed_by BIGINT REFERENCES users(id),
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 인덱스
CREATE INDEX idx_status_change_logs_user ON user_status_change_logs(user_id);
CREATE INDEX idx_status_change_logs_changed_at ON user_status_change_logs(changed_at DESC);
CREATE INDEX idx_role_change_logs_user ON user_role_change_logs(user_id);
CREATE INDEX idx_role_change_logs_changed_at ON user_role_change_logs(changed_at DESC);

-- 코멘트
COMMENT ON TABLE user_status_change_logs IS '사용자 상태 변경 로그';
COMMENT ON TABLE user_role_change_logs IS '사용자 역할 변경 로그';
