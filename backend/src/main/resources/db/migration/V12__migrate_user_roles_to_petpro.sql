-- PetPro 역할 체계 마이그레이션
-- USER → CUSTOMER, COMPANY_ADMIN/SUPPLIER_ADMIN 제거, PARTNER 추가

-- 1. 기존 USER 역할을 CUSTOMER로 변경
UPDATE users SET role = 'CUSTOMER' WHERE role = 'USER';

-- 2. 기존 COMPANY_ADMIN, SUPPLIER_ADMIN을 CUSTOMER로 변경 (deprecated 역할)
UPDATE users SET role = 'CUSTOMER' WHERE role IN ('COMPANY_ADMIN', 'SUPPLIER_ADMIN');

-- 3. CHECK 제약조건 변경
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_ck_role;
ALTER TABLE users ADD CONSTRAINT users_ck_role CHECK (role IN ('CUSTOMER', 'PARTNER', 'ADMIN', 'SUPER_ADMIN'));

-- 4. 컬럼 코멘트 업데이트
COMMENT ON COLUMN users.role IS '역할 (CUSTOMER, PARTNER, ADMIN, SUPER_ADMIN)';

-- 5. 로그인 잠금 및 Refresh Token 로테이션 컬럼 추가
ALTER TABLE users ADD COLUMN IF NOT EXISTS refresh_token VARCHAR(500);
ALTER TABLE users ADD COLUMN IF NOT EXISTS login_fail_count INTEGER DEFAULT 0;
ALTER TABLE users ADD COLUMN IF NOT EXISTS locked_at TIMESTAMP WITH TIME ZONE;

COMMENT ON COLUMN users.refresh_token IS '현재 유효한 Refresh Token (1회용 로테이션)';
COMMENT ON COLUMN users.login_fail_count IS '로그인 실패 횟수';
COMMENT ON COLUMN users.locked_at IS '계정 잠금 일시 (5회 실패 시 30분 잠금)';
