-- ============================================================
-- V5: 반려동물 테이블 생성
-- ============================================================

CREATE TABLE pets (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),

    -- 기본 정보
    name VARCHAR(100) NOT NULL,
    species VARCHAR(20) NOT NULL,
    breed VARCHAR(100),
    birth_date DATE,
    gender VARCHAR(10),
    is_neutered BOOLEAN DEFAULT FALSE,

    -- 프로필
    profile_image_url VARCHAR(500),
    memo TEXT,

    -- 사망 정보
    is_deceased BOOLEAN DEFAULT FALSE,
    deceased_at DATE,

    -- Soft Delete
    deleted_at TIMESTAMP,

    -- Audit
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 인덱스
CREATE INDEX idx_pets_user_id ON pets(user_id);
CREATE INDEX idx_pets_species ON pets(species);
CREATE INDEX idx_pets_is_deceased ON pets(is_deceased);
CREATE INDEX idx_pets_deleted_at ON pets(deleted_at) WHERE deleted_at IS NULL;

-- 코멘트
COMMENT ON TABLE pets IS '반려동물 정보';
COMMENT ON COLUMN pets.user_id IS '소유자(회원) ID';
COMMENT ON COLUMN pets.name IS '반려동물 이름';
COMMENT ON COLUMN pets.species IS '종류 (DOG, CAT, BIRD, HAMSTER, RABBIT, FISH, REPTILE, ETC)';
COMMENT ON COLUMN pets.breed IS '품종';
COMMENT ON COLUMN pets.birth_date IS '생년월일';
COMMENT ON COLUMN pets.gender IS '성별 (MALE, FEMALE, UNKNOWN)';
COMMENT ON COLUMN pets.is_neutered IS '중성화 여부';
COMMENT ON COLUMN pets.profile_image_url IS '프로필 이미지 URL';
COMMENT ON COLUMN pets.memo IS '메모';
COMMENT ON COLUMN pets.is_deceased IS '사망 여부';
COMMENT ON COLUMN pets.deceased_at IS '사망일';
COMMENT ON COLUMN pets.deleted_at IS '삭제일 (Soft Delete)';
