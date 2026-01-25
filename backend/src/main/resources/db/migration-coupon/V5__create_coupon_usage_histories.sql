-- 쿠폰 사용 이력 테이블
CREATE TABLE coupon_usage_histories (
    id BIGSERIAL PRIMARY KEY,
    user_coupon_id BIGINT NOT NULL REFERENCES user_coupons(id),
    user_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    discount_amount DECIMAL(10,2) NOT NULL,
    used_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 인덱스
CREATE INDEX idx_usage_histories_user_coupon ON coupon_usage_histories(user_coupon_id);
CREATE INDEX idx_usage_histories_user ON coupon_usage_histories(user_id);
CREATE INDEX idx_usage_histories_order ON coupon_usage_histories(order_id);

COMMENT ON TABLE coupon_usage_histories IS '쿠폰 사용 이력';
COMMENT ON COLUMN coupon_usage_histories.user_id IS '사용자 ID (Main DB 참조)';
COMMENT ON COLUMN coupon_usage_histories.order_id IS '주문 ID (Main DB 참조)';
COMMENT ON COLUMN coupon_usage_histories.discount_amount IS '할인 금액';
