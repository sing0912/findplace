# OAuth 소셜 로그인 설정 가이드

**최종 수정일:** 2026-02-05
**상태:** 확정

---

## 1. 개요

소셜 로그인(카카오, 네이버, 구글) 연동을 위한 OAuth Client ID 발급 및 설정 가이드입니다.

---

## 2. Google OAuth

### 2.1 발급 절차

1. **Google Cloud Console 접속**
   - URL: https://console.cloud.google.com

2. **프로젝트 생성**
   - 상단 프로젝트 선택 → 새 프로젝트
   - 프로젝트 이름: `petpro` (또는 원하는 이름)
   - 만들기 클릭

3. **OAuth 동의 화면 설정**
   - 메뉴: API 및 서비스 → OAuth 동의 화면
   - User Type: **외부** 선택
   - 앱 정보:
     - 앱 이름: `펫프`
     - 사용자 지원 이메일: 관리자 이메일
     - 개발자 연락처: 관리자 이메일
   - 범위: 기본값 (email, profile)
   - 저장 후 계속

4. **사용자 인증 정보 생성**
   - 메뉴: API 및 서비스 → 사용자 인증 정보
   - \+ 사용자 인증 정보 만들기 → OAuth 클라이언트 ID
   - 애플리케이션 유형: **웹 애플리케이션**
   - 이름: `petpro-web`

5. **URI 설정**

   | 환경 | 승인된 JavaScript 원본 | 승인된 리디렉션 URI |
   |------|----------------------|-------------------|
   | 로컬 | `http://localhost:3000` | `http://localhost:3000/oauth/google/callback` |
   | 운영 | `https://yourdomain.com` | `https://yourdomain.com/oauth/google/callback` |

6. **Client ID 복사**
   - 생성 완료 후 클라이언트 ID 복사
   - 클라이언트 보안 비밀번호는 백엔드에서 사용

### 2.2 환경 변수 설정

```bash
# frontend/.env
REACT_APP_GOOGLE_CLIENT_ID=your_google_client_id

# backend/.env (또는 application.yml)
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
```

---

## 3. Kakao OAuth

### 3.1 발급 절차

1. **Kakao Developers 접속**
   - URL: https://developers.kakao.com

2. **애플리케이션 생성**
   - 내 애플리케이션 → 애플리케이션 추가하기
   - 앱 이름: `펫프`
   - 사업자명: 회사명

3. **플랫폼 등록**
   - 앱 설정 → 플랫폼 → Web 플랫폼 등록
   - 사이트 도메인:
     - `http://localhost:3000`
     - `https://yourdomain.com`

4. **카카오 로그인 활성화**
   - 제품 설정 → 카카오 로그인 → 활성화 설정: ON
   - Redirect URI 등록:
     - `http://localhost:3000/oauth/kakao/callback`
     - `https://yourdomain.com/oauth/kakao/callback`

5. **동의항목 설정**
   - 제품 설정 → 카카오 로그인 → 동의항목
   - 필수: 닉네임, 이메일

6. **앱 키 확인**
   - 앱 설정 → 앱 키 → REST API 키 복사

### 3.2 환경 변수 설정

```bash
# frontend/.env
REACT_APP_KAKAO_CLIENT_ID=your_kakao_rest_api_key

# backend/.env
KAKAO_CLIENT_ID=your_kakao_rest_api_key
KAKAO_CLIENT_SECRET=your_kakao_client_secret  # 제품 설정 → 카카오 로그인 → 보안
```

---

## 4. Naver OAuth

### 4.1 발급 절차

1. **Naver Developers 접속**
   - URL: https://developers.naver.com

2. **애플리케이션 등록**
   - Application → 애플리케이션 등록
   - 애플리케이션 이름: `펫프`
   - 사용 API: 네이버 로그인
   - 제공 정보: 이메일, 이름, 프로필 사진

3. **환경 설정**
   - 환경 추가: PC 웹
   - 서비스 URL: `http://localhost:3000`
   - Callback URL:
     - `http://localhost:3000/oauth/naver/callback`
     - `https://yourdomain.com/oauth/naver/callback`

4. **Client ID/Secret 확인**
   - 애플리케이션 정보에서 Client ID, Client Secret 복사

### 4.2 환경 변수 설정

```bash
# frontend/.env
REACT_APP_NAVER_CLIENT_ID=your_naver_client_id

# backend/.env
NAVER_CLIENT_ID=your_naver_client_id
NAVER_CLIENT_SECRET=your_naver_client_secret
```

---

## 5. 환경별 설정 요약

### 5.1 Frontend (.env)

```bash
# 로컬 개발
REACT_APP_KAKAO_CLIENT_ID=xxx
REACT_APP_NAVER_CLIENT_ID=xxx
REACT_APP_GOOGLE_CLIENT_ID=xxx
```

### 5.2 Backend (application.yml)

```yaml
oauth:
  kakao:
    client-id: ${KAKAO_CLIENT_ID}
    client-secret: ${KAKAO_CLIENT_SECRET}
    redirect-uri: ${BASE_URL}/oauth/kakao/callback
  naver:
    client-id: ${NAVER_CLIENT_ID}
    client-secret: ${NAVER_CLIENT_SECRET}
    redirect-uri: ${BASE_URL}/oauth/naver/callback
  google:
    client-id: ${GOOGLE_CLIENT_ID}
    client-secret: ${GOOGLE_CLIENT_SECRET}
    redirect-uri: ${BASE_URL}/oauth/google/callback
```

---

## 6. 배포 체크리스트

- [ ] 각 OAuth 제공자에 운영 도메인 등록
- [ ] 운영 환경 Redirect URI 추가
- [ ] 환경 변수 설정 (서버)
- [ ] HTTPS 적용 확인
- [ ] 테스트 계정으로 로그인 테스트

---

## 7. 트러블슈팅

### "redirect_uri_mismatch" 오류
- OAuth 설정의 Redirect URI와 실제 호출 URI가 정확히 일치하는지 확인
- 끝에 슬래시(/) 유무 확인

### "client_id is not configured" 오류
- .env 파일에 환경 변수가 설정되어 있는지 확인
- 개발 서버 재시작 필요 (환경 변수 변경 시)

### "access_denied" 오류
- OAuth 동의 화면 설정 확인
- 테스트 모드인 경우 테스트 사용자 등록 필요 (Google)
