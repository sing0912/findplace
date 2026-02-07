-- =====================================================
-- V11: 사용자 인증 및 문의 기능 추가
-- - users 테이블 컬럼 추가 (nickname, provider, agreements)
-- - verification_requests 테이블 생성
-- - inquiries, inquiry_answers 테이블 생성
-- =====================================================

-- 1. users 테이블 컬럼 추가
ALTER TABLE users ADD COLUMN IF NOT EXISTS nickname VARCHAR(50);
ALTER TABLE users ADD COLUMN IF NOT EXISTS provider VARCHAR(20) DEFAULT 'EMAIL';
ALTER TABLE users ADD COLUMN IF NOT EXISTS provider_id VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS agree_terms BOOLEAN DEFAULT false;
ALTER TABLE users ADD COLUMN IF NOT EXISTS agree_privacy BOOLEAN DEFAULT false;
ALTER TABLE users ADD COLUMN IF NOT EXISTS agree_marketing BOOLEAN DEFAULT false;

-- nickname 인덱스 추가
CREATE INDEX IF NOT EXISTS idx_users_nickname ON users(nickname);

-- provider + provider_id 인덱스 추가 (소셜 로그인 조회용)
CREATE INDEX IF NOT EXISTS idx_users_provider_provider_id ON users(provider, provider_id);

-- 2. verification_requests 테이블 생성 (SMS 인증)
CREATE TABLE IF NOT EXISTS verification_requests (
    id VARCHAR(36) PRIMARY KEY,
    type VARCHAR(20) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    code VARCHAR(10) NOT NULL,
    user_id BIGINT,
    name VARCHAR(100),
    email VARCHAR(255),
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    verified BOOLEAN NOT NULL DEFAULT false,
    reset_token VARCHAR(36),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT verification_requests_fk_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT verification_requests_ck_type
        CHECK (type IN ('FIND_ID', 'RESET_PASSWORD'))
);

-- verification_requests 인덱스
CREATE INDEX IF NOT EXISTS idx_verification_requests_phone_type
    ON verification_requests(phone, type);
CREATE INDEX IF NOT EXISTS idx_verification_requests_reset_token
    ON verification_requests(reset_token) WHERE reset_token IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_verification_requests_expires_at
    ON verification_requests(expires_at);

-- 3. inquiries 테이블 생성 (문의 게시판)
CREATE TABLE IF NOT EXISTS inquiries (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'WAITING',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT inquiries_fk_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT inquiries_ck_status
        CHECK (status IN ('WAITING', 'ANSWERED'))
);

-- inquiries 인덱스
CREATE INDEX IF NOT EXISTS idx_inquiries_user_id ON inquiries(user_id);
CREATE INDEX IF NOT EXISTS idx_inquiries_status ON inquiries(status);
CREATE INDEX IF NOT EXISTS idx_inquiries_created_at ON inquiries(created_at DESC);

-- 4. inquiry_answers 테이블 생성 (문의 답변)
CREATE TABLE IF NOT EXISTS inquiry_answers (
    id BIGSERIAL PRIMARY KEY,
    inquiry_id BIGINT NOT NULL UNIQUE,
    admin_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT inquiry_answers_fk_inquiry
        FOREIGN KEY (inquiry_id) REFERENCES inquiries(id) ON DELETE CASCADE,
    CONSTRAINT inquiry_answers_fk_admin
        FOREIGN KEY (admin_id) REFERENCES users(id) ON DELETE RESTRICT
);

-- inquiry_answers 인덱스
CREATE INDEX IF NOT EXISTS idx_inquiry_answers_inquiry_id ON inquiry_answers(inquiry_id);
CREATE INDEX IF NOT EXISTS idx_inquiry_answers_admin_id ON inquiry_answers(admin_id);

-- 코멘트 추가
COMMENT ON TABLE verification_requests IS 'SMS 인증 요청 (아이디 찾기, 비밀번호 재설정)';
COMMENT ON TABLE inquiries IS '사용자 문의 게시판';
COMMENT ON TABLE inquiry_answers IS '문의 답변 (관리자)';

COMMENT ON COLUMN users.nickname IS '사용자 닉네임';
COMMENT ON COLUMN users.provider IS '인증 제공자 (EMAIL, KAKAO, NAVER, GOOGLE)';
COMMENT ON COLUMN users.provider_id IS '소셜 로그인 제공자 ID';
COMMENT ON COLUMN users.agree_terms IS '이용약관 동의 여부';
COMMENT ON COLUMN users.agree_privacy IS '개인정보처리방침 동의 여부';
COMMENT ON COLUMN users.agree_marketing IS '마케팅 정보 수신 동의 여부';
