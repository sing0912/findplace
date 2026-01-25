-- 쿠폰 조건 테이블 (EAV 패턴)
CREATE TABLE coupon_conditions (
    id BIGSERIAL PRIMARY KEY,
    coupon_id BIGINT NOT NULL REFERENCES coupons(id) ON DELETE CASCADE,
    condition_key VARCHAR(50) NOT NULL,
    condition_operator VARCHAR(20) NOT NULL,
    condition_value VARCHAR(500) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 인덱스
CREATE INDEX idx_coupon_conditions_coupon ON coupon_conditions(coupon_id);

COMMENT ON TABLE coupon_conditions IS '쿠폰 조건 (EAV)';
COMMENT ON COLUMN coupon_conditions.condition_key IS '조건 키 (MIN_ORDER_AMOUNT, CATEGORY 등)';
COMMENT ON COLUMN coupon_conditions.condition_operator IS '조건 연산자 (EQ, GTE, IN 등)';
COMMENT ON COLUMN coupon_conditions.condition_value IS '조건 값';
