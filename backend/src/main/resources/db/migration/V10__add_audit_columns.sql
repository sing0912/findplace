-- 감사 컬럼 추가 (BaseEntity 필드)
-- created_by, updated_by 컬럼이 누락된 테이블에 추가

-- funeral_homes 테이블
ALTER TABLE funeral_homes ADD COLUMN IF NOT EXISTS created_by BIGINT;
ALTER TABLE funeral_homes ADD COLUMN IF NOT EXISTS updated_by BIGINT;

-- pets 테이블
ALTER TABLE pets ADD COLUMN IF NOT EXISTS created_by BIGINT;
ALTER TABLE pets ADD COLUMN IF NOT EXISTS updated_by BIGINT;

-- 코멘트
COMMENT ON COLUMN funeral_homes.created_by IS '생성자 ID';
COMMENT ON COLUMN funeral_homes.updated_by IS '수정자 ID';
COMMENT ON COLUMN pets.created_by IS '생성자 ID';
COMMENT ON COLUMN pets.updated_by IS '수정자 ID';
