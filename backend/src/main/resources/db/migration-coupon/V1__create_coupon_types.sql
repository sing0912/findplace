-- 쿠폰 유형 테이블
CREATE TABLE coupon_types (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 기본 쿠폰 유형 데이터
INSERT INTO coupon_types (code, name, description) VALUES
    ('FIXED', '정액 할인', '고정 금액 할인 (예: 5,000원)'),
    ('PERCENT', '정률 할인', '퍼센트 할인 (예: 10%)'),
    ('SHIPPING', '배송비 할인', '배송비 무료/할인'),
    ('PERIOD', '기간 할인', '특정 기간 동안 할인'),
    ('BULK', '대량구매 할인', 'N개 이상 구매 시 할인'),
    ('AMOUNT', '금액별 할인', 'N원 이상 구매 시 할인'),
    ('FIRST_ORDER', '첫 주문 할인', '첫 주문 고객 전용'),
    ('BIRTHDAY', '생일 할인', '생일 기념 할인');

COMMENT ON TABLE coupon_types IS '쿠폰 유형';
COMMENT ON COLUMN coupon_types.code IS '유형 코드';
COMMENT ON COLUMN coupon_types.name IS '유형명';
COMMENT ON COLUMN coupon_types.description IS '설명';
