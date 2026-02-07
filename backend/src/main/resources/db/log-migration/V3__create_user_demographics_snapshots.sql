-- 인구통계 스냅샷 테이블
CREATE TABLE user_demographics_snapshots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '사용자 ID',
    age_group VARCHAR(10) COMMENT '연령대',
    gender VARCHAR(10) COMMENT '성별',
    region_code VARCHAR(20) COMMENT '지역코드',
    snapshot_date DATE NOT NULL COMMENT '스냅샷 날짜',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '생성일시',

    UNIQUE KEY uk_user_demographics_user_date (user_id, snapshot_date),
    INDEX idx_user_demographics_snapshot_date (snapshot_date),
    INDEX idx_user_demographics_age_group (age_group),
    INDEX idx_user_demographics_gender (gender),
    INDEX idx_user_demographics_region (region_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='인구통계 스냅샷';
