-- 사용자 테이블 필드 확장
ALTER TABLE users ADD COLUMN IF NOT EXISTS birth_date DATE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS address VARCHAR(500);
ALTER TABLE users ADD COLUMN IF NOT EXISTS address_detail VARCHAR(200);
ALTER TABLE users ADD COLUMN IF NOT EXISTS zip_code VARCHAR(10);
ALTER TABLE users ADD COLUMN IF NOT EXISTS latitude DECIMAL(10, 7);
ALTER TABLE users ADD COLUMN IF NOT EXISTS longitude DECIMAL(10, 7);

-- 인덱스
CREATE INDEX IF NOT EXISTS idx_users_birth_date ON users(birth_date);

-- 코멘트
COMMENT ON COLUMN users.birth_date IS '생년월일';
COMMENT ON COLUMN users.address IS '기본 주소';
COMMENT ON COLUMN users.address_detail IS '상세 주소';
COMMENT ON COLUMN users.zip_code IS '우편번호';
COMMENT ON COLUMN users.latitude IS '위도';
COMMENT ON COLUMN users.longitude IS '경도';
