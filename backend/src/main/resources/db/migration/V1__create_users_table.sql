-- ============================================================
-- V1: Users Table
-- ============================================================

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    profile_image_url TEXT,
    last_login_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    deleted_at TIMESTAMP WITH TIME ZONE,
    deleted_by BIGINT,
    CONSTRAINT users_uq_email UNIQUE (email),
    CONSTRAINT users_ck_role CHECK (role IN ('USER', 'COMPANY_ADMIN', 'SUPPLIER_ADMIN', 'ADMIN', 'SUPER_ADMIN')),
    CONSTRAINT users_ck_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'DELETED'))
);

-- Indexes
CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_phone ON users (phone);
CREATE INDEX idx_users_role ON users (role);
CREATE INDEX idx_users_status ON users (status);
CREATE INDEX idx_users_created_at ON users (created_at);

-- Comments
COMMENT ON TABLE users IS '사용자 테이블';
COMMENT ON COLUMN users.id IS '사용자 ID';
COMMENT ON COLUMN users.email IS '이메일 (로그인 ID)';
COMMENT ON COLUMN users.password IS '암호화된 비밀번호';
COMMENT ON COLUMN users.name IS '이름';
COMMENT ON COLUMN users.phone IS '전화번호';
COMMENT ON COLUMN users.role IS '역할 (USER, COMPANY_ADMIN, SUPPLIER_ADMIN, ADMIN, SUPER_ADMIN)';
COMMENT ON COLUMN users.status IS '상태 (ACTIVE, INACTIVE, SUSPENDED, DELETED)';
