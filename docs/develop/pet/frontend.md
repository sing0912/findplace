# 펫 관리 프론트엔드

## 개요

마이페이지 > 펫 관리 기능의 프론트엔드 구현 지침.
반려동물 CRUD와 성향 체크리스트 관리를 MUI 기반으로 구현합니다.

---

## IA 화면 구조

| 화면 ID | 화면명 | 경로 | 설명 |
|---------|--------|------|------|
| U-PET-001 | 펫 리스트 | `/mypage/pets` | 등록된 반려동물 목록 |
| U-PET-002 | 펫 등록 | `/mypage/pets/register` | 반려동물 신규 등록 |
| U-PET-003 | 펫 수정 | `/mypage/pets/:id/edit` | 반려동물 정보 수정 |
| U-PET-004 | 성향 체크리스트 | `/mypage/pets/:id/checklist` | 성향 체크리스트 작성/수정 |

---

## 화면별 스펙

### U-PET-001: 펫 리스트

- **헤더:** "내 반려동물" + 뒤로가기 (마이페이지)
- **통계 영역:** 전체 N마리 / 함께하는 N마리 / 무지개다리 N마리 (Chip 사용)
- **펫 카드 리스트:** Grid 레이아웃, PetCard 컴포넌트
- **빈 상태:** 이모지 + "등록된 반려동물이 없습니다" + 등록 버튼
- **등록 버튼:** Fab (FloatingActionButton) - 최대 10마리 제한 시 비활성화
- **삭제:** 확인 Dialog 후 삭제

### U-PET-002: 펫 등록

- **헤더:** "반려동물 등록" + 뒤로가기
- **폼:** PetForm 컴포넌트 (MUI)
- **등록 성공:** 이미지 업로드(있으면) → 리스트로 navigate
- **10마리 초과:** 등록 페이지 진입 시 경고 후 리스트로 리다이렉트

### U-PET-003: 펫 수정

- **헤더:** "반려동물 수정" + 뒤로가기
- **폼:** PetForm 컴포넌트 (기존 데이터 로드)
- **하단:** "성향 체크리스트 작성" 링크 버튼
- **수정 성공:** 이미지 업로드(있으면) → 리스트로 navigate

### U-PET-004: 성향 체크리스트

- **헤더:** "성향 체크리스트" + 뒤로가기
- **폼:** PetChecklistForm 컴포넌트 (MUI Slider/Select)
- **기존 데이터:** usePetChecklist 훅으로 로드 (있으면 수정 모드)
- **저장 성공:** 수정 페이지로 navigate

---

## API 연동 스펙

### Pet API (`api/pet.ts`)

| 함수 | Method | 엔드포인트 | 설명 |
|------|--------|-----------|------|
| `getMyPets()` | GET | `/pets` | 내 반려동물 목록 |
| `getPet(id)` | GET | `/pets/{id}` | 상세 조회 |
| `createPet(req)` | POST | `/pets` | 등록 |
| `updatePet(id, req)` | PUT | `/pets/{id}` | 수정 |
| `deletePet(id)` | DELETE | `/pets/{id}` | 삭제 |
| `uploadPetImage(id, file)` | POST | `/pets/{id}/image` | 이미지 업로드 |

### PetChecklist API (`api/petChecklist.ts`)

| 함수 | Method | 엔드포인트 | 설명 |
|------|--------|-----------|------|
| `getChecklist(petId)` | GET | `/pets/{id}/checklist` | 체크리스트 조회 |
| `createChecklist(petId, req)` | POST | `/pets/{id}/checklist` | 체크리스트 생성 |
| `updateChecklist(petId, req)` | PUT | `/pets/{id}/checklist` | 체크리스트 수정 |

---

## 라우팅 테이블

```tsx
<Route path="mypage/pets" element={<PetListPage />} />
<Route path="mypage/pets/register" element={<PetRegisterPage />} />
<Route path="mypage/pets/:id/edit" element={<PetEditPage />} />
<Route path="mypage/pets/:id/checklist" element={<PetChecklistPage />} />
```

---

## 컴포넌트 구조

```
frontend/src/
├── types/
│   ├── pet.ts              # Pet 타입 (기존)
│   └── petChecklist.ts     # PetChecklist 타입 (신규)
├── api/
│   ├── pet.ts              # Pet API (기존)
│   └── petChecklist.ts     # PetChecklist API (신규)
├── hooks/
│   ├── usePets.ts          # Pet 훅 (기존)
│   └── usePetChecklist.ts  # PetChecklist 훅 (신규)
├── components/pet/
│   ├── PetCard.tsx         # 펫 카드 (MUI 전환)
│   ├── PetForm.tsx         # 펫 폼 (MUI 전환)
│   ├── PetChecklistForm.tsx # 체크리스트 폼 (신규)
│   └── index.ts            # barrel export
└── pages/pet/
    ├── PetListPage.tsx     # 목록 페이지 (MUI 전환)
    ├── PetRegisterPage.tsx # 등록 페이지 (신규)
    ├── PetEditPage.tsx     # 수정 페이지 (신규)
    ├── PetChecklistPage.tsx # 체크리스트 페이지 (신규)
    └── index.ts            # barrel export
```

---

## 디자인 시스템

### 색상 팔레트 (프로젝트 공통)

| 용도 | 색상 코드 |
|------|----------|
| Primary | `#76BCA2` |
| Primary Hover | `#5FA88E` |
| Background Light | `#F5FAF8` |
| Text Primary | `#000000` |
| Text Secondary | `#404040` |
| Text Disabled | `#AEAEAE` |
| Error | `#FF0000` |
| Card Border Radius | `12px` |

### MUI 컴포넌트 사용

- **Card/CardContent**: PetCard
- **Avatar**: 프로필 이미지 또는 종류 이모지
- **Typography**: 텍스트 표시
- **IconButton**: 수정/삭제 버튼
- **Chip**: 통계, 무지개다리 배지
- **TextField**: 입력 필드
- **Select/MenuItem**: 드롭다운
- **RadioGroup/Radio**: 성별 선택
- **Checkbox/FormControlLabel**: 중성화 여부
- **Slider**: 체크리스트 수치 항목 (1~5)
- **Fab**: 등록 플로팅 버튼
- **Dialog**: 삭제 확인
- **Container/Box/Grid**: 레이아웃
- **AuthButton**: 폼 하단 버튼 (기존 컴포넌트 재사용)
