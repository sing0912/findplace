-- 사용자 보유 쿠폰 테이블
CREATE TABLE user_coupons (
    id BIGSERIAL PRIMARY KEY,
    coupon_id BIGINT NOT NULL REFERENCES coupons(id),
    user_id BIGINT NOT NULL,

    -- 상태
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',

    -- 일시
    issued_at TIMESTAMP NOT NULL,
    expired_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP,
    revoked_at TIMESTAMP,

    -- 사용 정보
    order_id BIGINT,

    -- Audit
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 인덱스
CREATE INDEX idx_user_coupons_user ON user_coupons(user_id);
CREATE INDEX idx_user_coupons_coupon ON user_coupons(coupon_id);
CREATE INDEX idx_user_coupons_status ON user_coupons(status);
CREATE INDEX idx_user_coupons_expired ON user_coupons(expired_at) WHERE status = 'AVAILABLE';

COMMENT ON TABLE user_coupons IS '사용자 보유 쿠폰';
COMMENT ON COLUMN user_coupons.user_id IS '사용자 ID (Main DB 참조)';
COMMENT ON COLUMN user_coupons.status IS '상태 (AVAILABLE, USED, EXPIRED, REVOKED)';
COMMENT ON COLUMN user_coupons.order_id IS '사용한 주문 ID (Main DB 참조)';
