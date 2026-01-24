-- ============================================================
-- V2: Companies Table
-- ============================================================

CREATE TABLE companies (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    business_number VARCHAR(20) NOT NULL,
    representative_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(255),
    address TEXT NOT NULL,
    address_detail TEXT,
    zipcode VARCHAR(10),
    latitude NUMERIC(10, 7),
    longitude NUMERIC(10, 7),
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    verified_at TIMESTAMP WITH TIME ZONE,
    rating NUMERIC(3, 2) DEFAULT 0,
    review_count INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    deleted_at TIMESTAMP WITH TIME ZONE,
    deleted_by BIGINT,
    CONSTRAINT companies_uq_business_number UNIQUE (business_number),
    CONSTRAINT companies_ck_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'SUSPENDED', 'DELETED'))
);

-- Indexes
CREATE INDEX idx_companies_name ON companies (name);
CREATE INDEX idx_companies_status ON companies (status);
CREATE INDEX idx_companies_is_verified ON companies (is_verified);
CREATE INDEX idx_companies_location ON companies (latitude, longitude);
CREATE INDEX idx_companies_rating ON companies (rating DESC);

-- Comments
COMMENT ON TABLE companies IS '장례업체 테이블';
COMMENT ON COLUMN companies.id IS '업체 ID';
COMMENT ON COLUMN companies.name IS '업체명';
COMMENT ON COLUMN companies.business_number IS '사업자등록번호';
COMMENT ON COLUMN companies.rating IS '평균 평점 (0.00 ~ 5.00)';
