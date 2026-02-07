-- 사용자 행위 로그 테이블
CREATE TABLE user_action_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '사용자 ID',
    action_type VARCHAR(50) NOT NULL COMMENT '행위 유형',
    target_type VARCHAR(50) COMMENT '대상 유형',
    target_id BIGINT COMMENT '대상 ID',
    description TEXT COMMENT '설명',
    detail_json JSON COMMENT '상세 JSON 데이터',
    ip_address VARCHAR(45) COMMENT 'IP 주소',
    user_agent VARCHAR(500) COMMENT 'UserAgent',
    device_type VARCHAR(20) COMMENT '디바이스 유형',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '생성일시',

    INDEX idx_user_action_logs_user_id (user_id),
    INDEX idx_user_action_logs_action_type (action_type),
    INDEX idx_user_action_logs_target (target_type, target_id),
    INDEX idx_user_action_logs_created_at (created_at),
    INDEX idx_user_action_logs_device_type (device_type),
    INDEX idx_user_action_logs_user_created (user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자 행위 로그';
