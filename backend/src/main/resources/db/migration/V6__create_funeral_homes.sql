-- 장례식장 테이블
CREATE TABLE funeral_homes (
    id BIGSERIAL PRIMARY KEY,

    -- 기본 정보
    name VARCHAR(200) NOT NULL,
    road_address VARCHAR(500),
    lot_address VARCHAR(500),
    phone VARCHAR(50),

    -- 지역 정보
    loc_code VARCHAR(20),
    loc_name VARCHAR(100),

    -- 서비스 유형
    has_crematorium BOOLEAN NOT NULL DEFAULT FALSE,
    has_columbarium BOOLEAN NOT NULL DEFAULT FALSE,
    has_funeral BOOLEAN NOT NULL DEFAULT FALSE,

    -- 좌표 (Geocoding)
    latitude DECIMAL(10, 7),
    longitude DECIMAL(10, 7),
    geocoded_at TIMESTAMP,

    -- 상태
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    verified_at TIMESTAMP,
    synced_at TIMESTAMP,

    -- Audit
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 인덱스
CREATE INDEX idx_funeral_homes_loc_code ON funeral_homes(loc_code);
CREATE INDEX idx_funeral_homes_is_active ON funeral_homes(is_active);
CREATE INDEX idx_funeral_homes_name ON funeral_homes(name);
CREATE INDEX idx_funeral_homes_location ON funeral_homes(latitude, longitude);
CREATE INDEX idx_funeral_homes_services ON funeral_homes(has_crematorium, has_funeral, has_columbarium);

-- 동기화 로그 테이블
CREATE TABLE funeral_home_sync_logs (
    id BIGSERIAL PRIMARY KEY,
    sync_type VARCHAR(20) NOT NULL,
    started_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    status VARCHAR(20) NOT NULL,
    total_count INTEGER DEFAULT 0,
    inserted_count INTEGER DEFAULT 0,
    updated_count INTEGER DEFAULT 0,
    deleted_count INTEGER DEFAULT 0,
    error_count INTEGER DEFAULT 0,
    error_message TEXT
);

-- 동기화 로그 인덱스
CREATE INDEX idx_sync_logs_sync_type ON funeral_home_sync_logs(sync_type);
CREATE INDEX idx_sync_logs_started_at ON funeral_home_sync_logs(started_at DESC);
CREATE INDEX idx_sync_logs_status ON funeral_home_sync_logs(status);

-- 코멘트
COMMENT ON TABLE funeral_homes IS '장례식장 정보';
COMMENT ON COLUMN funeral_homes.name IS '장례식장 이름';
COMMENT ON COLUMN funeral_homes.road_address IS '도로명 주소';
COMMENT ON COLUMN funeral_homes.lot_address IS '지번 주소';
COMMENT ON COLUMN funeral_homes.phone IS '전화번호';
COMMENT ON COLUMN funeral_homes.loc_code IS '지역 코드 (공공API)';
COMMENT ON COLUMN funeral_homes.loc_name IS '지역명';
COMMENT ON COLUMN funeral_homes.has_crematorium IS '화장장 보유 여부';
COMMENT ON COLUMN funeral_homes.has_columbarium IS '납골당 보유 여부';
COMMENT ON COLUMN funeral_homes.has_funeral IS '장례식장 보유 여부';
COMMENT ON COLUMN funeral_homes.latitude IS '위도';
COMMENT ON COLUMN funeral_homes.longitude IS '경도';
COMMENT ON COLUMN funeral_homes.geocoded_at IS 'Geocoding 완료 시간';
COMMENT ON COLUMN funeral_homes.is_active IS '활성화 여부';
COMMENT ON COLUMN funeral_homes.verified_at IS '검증 완료 시간';
COMMENT ON COLUMN funeral_homes.synced_at IS '마지막 동기화 시간';

COMMENT ON TABLE funeral_home_sync_logs IS '장례식장 동기화 로그';
COMMENT ON COLUMN funeral_home_sync_logs.sync_type IS '동기화 유형 (INCREMENTAL, FULL)';
COMMENT ON COLUMN funeral_home_sync_logs.status IS '동기화 상태 (RUNNING, COMPLETED, FAILED, PARTIAL)';
