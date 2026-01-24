-- ============================================================
-- V3: Suppliers Table
-- ============================================================

CREATE TABLE suppliers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    business_number VARCHAR(20) NOT NULL,
    representative_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(255),
    address TEXT NOT NULL,
    address_detail TEXT,
    zipcode VARCHAR(10),
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    verified_at TIMESTAMP WITH TIME ZONE,
    bank_name VARCHAR(50),
    bank_account_number VARCHAR(50),
    bank_account_holder VARCHAR(100),
    settlement_day INTEGER DEFAULT 15,
    commission_rate NUMERIC(5, 2) DEFAULT 10.00,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    deleted_at TIMESTAMP WITH TIME ZONE,
    deleted_by BIGINT,
    CONSTRAINT suppliers_uq_business_number UNIQUE (business_number),
    CONSTRAINT suppliers_ck_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'SUSPENDED', 'DELETED')),
    CONSTRAINT suppliers_ck_settlement_day CHECK (settlement_day BETWEEN 1 AND 28),
    CONSTRAINT suppliers_ck_commission_rate CHECK (commission_rate BETWEEN 0 AND 100)
);

-- Indexes
CREATE INDEX idx_suppliers_name ON suppliers (name);
CREATE INDEX idx_suppliers_status ON suppliers (status);
CREATE INDEX idx_suppliers_is_verified ON suppliers (is_verified);

-- Comments
COMMENT ON TABLE suppliers IS '공급사 테이블';
COMMENT ON COLUMN suppliers.settlement_day IS '정산일 (매월 N일)';
COMMENT ON COLUMN suppliers.commission_rate IS '수수료율 (%)';
