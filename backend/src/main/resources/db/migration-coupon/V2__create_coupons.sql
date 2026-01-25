-- 쿠폰 마스터 테이블
CREATE TABLE coupons (
    id BIGSERIAL PRIMARY KEY,

    -- 기본 정보
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    coupon_type_id BIGINT REFERENCES coupon_types(id),

    -- 할인 설정
    discount_method VARCHAR(20) NOT NULL,
    discount_value DECIMAL(10,2) NOT NULL,
    max_discount_amount DECIMAL(10,2),

    -- 발급 설정
    issue_type VARCHAR(20) NOT NULL DEFAULT 'MANUAL',
    auto_issue_event VARCHAR(30),
    max_issue_count INT,
    issued_count INT NOT NULL DEFAULT 0,
    max_per_user INT NOT NULL DEFAULT 1,

    -- 유효 기간
    valid_start_date DATE,
    valid_end_date DATE,
    valid_days INT,

    -- 옵션
    is_stackable BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    -- Audit
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 인덱스
CREATE INDEX idx_coupons_type ON coupons(coupon_type_id);
CREATE INDEX idx_coupons_issue_type ON coupons(issue_type);
CREATE INDEX idx_coupons_auto_event ON coupons(auto_issue_event) WHERE auto_issue_event IS NOT NULL;
CREATE INDEX idx_coupons_active ON coupons(is_active);

COMMENT ON TABLE coupons IS '쿠폰 마스터';
COMMENT ON COLUMN coupons.code IS '쿠폰 코드';
COMMENT ON COLUMN coupons.discount_method IS '할인 방식 (FIXED, PERCENT, FREE)';
COMMENT ON COLUMN coupons.issue_type IS '발급 유형 (MANUAL, CODE, AUTO)';
COMMENT ON COLUMN coupons.auto_issue_event IS '자동 발급 이벤트';
