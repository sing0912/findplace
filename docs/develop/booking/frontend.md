# Booking 도메인 - 프론트엔드 지침

**최종 수정일:** 2026-02-07
**상태:** 확정
**Phase:** 2 (예약/결제)

---

## 1. 개요

Booking 도메인의 프론트엔드 화면 구현 영구 지침입니다. 반려인의 예약 요청 4단계 프로세스, 예약 내역 관리, 시터의 예약관리 화면을 포함합니다.

### 1.1 Figma 참조
- **URL:** https://www.figma.com/design/mXrXb73tJYn0qzE9jKgEUv/펫프로-와이어프레임

### 1.2 기능 목록

| # | 기능 | 사용자 | Figma |
|---|------|--------|-------|
| 1 | 예약 요청 4단계 | 반려인 | 구현 후 추가 |
| 2 | 예약 완료 화면 | 반려인 | 구현 후 추가 |
| 3 | 예약 내역 (탭) | 반려인 | 구현 후 추가 |
| 4 | 예약 상세 | 반려인 | 구현 후 추가 |
| 5 | 시터 예약관리 | 시터 | 구현 후 추가 |

---

## 2. 파일 구조

```
frontend/src/
├── components/
│   └── booking/
│       ├── BookingStepIndicator.tsx       # 4단계 스텝 인디케이터
│       ├── DateTimePicker.tsx             # 일정 선택 컴포넌트
│       ├── PetSelector.tsx               # 펫 선택 카드 (체크박스)
│       ├── QuoteSummary.tsx              # 견적 요약 카드
│       ├── BookingCard.tsx               # 예약 내역 카드
│       ├── BookingStatusBadge.tsx        # 상태 배지
│       ├── BookingDetailInfo.tsx         # 예약 상세 정보 블록
│       ├── BookingStatusTimeline.tsx     # 상태 이력 타임라인
│       ├── PartnerBookingCard.tsx        # 시터용 예약 카드 (수락/거절 버튼)
│       ├── RejectReasonModal.tsx         # 거절 사유 입력 모달
│       └── index.ts
│
├── pages/
│   ├── booking/
│   │   ├── BookingStep1Page.tsx          # Step 1: 일정 선택
│   │   ├── BookingStep2Page.tsx          # Step 2: 펫 선택
│   │   ├── BookingStep3Page.tsx          # Step 3: 견적 확인
│   │   ├── BookingStep4Page.tsx          # Step 4: 요청사항 + 결제
│   │   ├── BookingCompletePage.tsx       # 예약 요청 완료
│   │   ├── BookingListPage.tsx           # 예약 내역 (탭)
│   │   ├── BookingDetailPage.tsx         # 예약 상세
│   │   └── index.ts
│   └── partner/
│       ├── PartnerBookingListPage.tsx    # 시터 예약관리
│       ├── PartnerBookingDetailPage.tsx  # 시터 예약 상세
│       └── index.ts
│
├── hooks/
│   └── useBooking.ts                     # 예약 관련 커스텀 훅
│
└── api/
    └── booking.ts                        # 예약 API 서비스
```

---

## 3. 라우팅

### 3.1 반려인 라우트

| 경로 | 컴포넌트 | 인증 | 설명 |
|------|----------|------|------|
| /booking/step1 | BookingStep1Page | CUSTOMER | 일정 선택 |
| /booking/step2 | BookingStep2Page | CUSTOMER | 펫 선택 |
| /booking/step3 | BookingStep3Page | CUSTOMER | 견적 확인 |
| /booking/step4 | BookingStep4Page | CUSTOMER | 요청사항 + 결제 |
| /booking/complete | BookingCompletePage | CUSTOMER | 예약 요청 완료 |
| /bookings | BookingListPage | CUSTOMER | 예약 내역 |
| /bookings/:id | BookingDetailPage | CUSTOMER | 예약 상세 |

### 3.2 시터 라우트

| 경로 | 컴포넌트 | 인증 | 설명 |
|------|----------|------|------|
| /partner/bookings | PartnerBookingListPage | PARTNER | 시터 예약관리 |
| /partner/bookings/:id | PartnerBookingDetailPage | PARTNER | 시터 예약 상세 |

---

## 4. 예약 4단계 UI 레이아웃

### 4.1 Step 1: 일정 선택 (BookingStep1Page)

**경로:** `/booking/step1?sitterId={sitterId}`

**구성:**
- BookingStepIndicator (1/4 활성)
- 시터 정보 요약 카드 (이름, 사진, 평점)
- 서비스 유형 선택 (라디오 버튼)
  - 데이케어 / 위탁 돌봄 / 산책 / 목욕미용 / 훈련
- DateTimePicker (시작일시 / 종료일시)
- 다음 버튼

**유효성 검증:**
- 서비스 유형 필수 선택
- 시작일시 필수
- 종료일시 필수, 시작일시 이후
- 시터 가용일 확인 (API 호출)

### 4.2 Step 2: 펫 선택 (BookingStep2Page)

**경로:** `/booking/step2`

**구성:**
- BookingStepIndicator (2/4 활성)
- 내 반려동물 리스트 (체크박스 카드)
  - 각 카드: 사진, 이름, 품종, 나이, 몸무게
  - 복수 선택 가능
- 선택된 반려동물 수 표시
- 이전 / 다음 버튼

**유효성 검증:**
- 최소 1마리 이상 선택 필수

### 4.3 Step 3: 견적 확인 (BookingStep3Page)

**경로:** `/booking/step3`

**구성:**
- BookingStepIndicator (3/4 활성)
- 예약 정보 요약
  - 시터 정보
  - 서비스 유형
  - 날짜/시간
  - 선택된 반려동물 목록
- QuoteSummary (견적 상세)
  - 기본 가격: 40,000원
  - 추가 반려동물 (N마리): +15,000원
  - 합계: 55,000원
- 이전 / 다음 버튼

### 4.4 Step 4: 요청사항 + 결제 (BookingStep4Page)

**경로:** `/booking/step4`

**구성:**
- BookingStepIndicator (4/4 활성)
- 요청사항 입력 (textarea, 최대 500자)
  - placeholder: "시터에게 전달할 요청사항을 입력해주세요."
  - 글자수 카운터 표시
- 최종 결제 금액 표시
- 결제 수단 선택 영역 (PaymentPage로 연동)
- 이전 / 예약 요청하기 버튼

### 4.5 예약 요청 완료 (BookingCompletePage)

**경로:** `/booking/complete`

**구성:**
- 체크 아이콘 (성공)
- "예약 요청이 완료되었습니다." 메시지
- 예약번호 표시
- "시터의 수락을 기다려주세요." 안내
- 예약 내역 보기 버튼 → `/bookings`
- 홈으로 가기 버튼 → `/`

---

## 5. 예약 내역 UI (BookingListPage)

**경로:** `/bookings`

### 5.1 탭 구성

| 탭 | 상태 필터 | 설명 |
|----|----------|------|
| 대기 | REQUESTED | 시터 응답 대기 중 |
| 예정 | ACCEPTED, CONFIRMED | 수락/결제 완료, 돌봄 전 |
| 진행중 | IN_PROGRESS | 돌봄 진행 중 |
| 완료 | COMPLETED | 돌봄 완료 |
| 취소 | CANCELLED, REJECTED | 취소/거절됨 |

### 5.2 BookingCard 구성

각 예약 카드에 포함되는 정보:
- 예약번호
- 시터 이름 + 프로필 이미지
- 서비스 유형
- 날짜/시간
- 반려동물 이름 목록
- 금액
- 상태 배지 (BookingStatusBadge)
- 카드 탭 → 예약 상세 이동

### 5.3 BookingStatusBadge 색상

| 상태 | 색상 | 텍스트 |
|------|------|--------|
| REQUESTED | #FFA500 (주황) | 대기중 |
| ACCEPTED | #4A90D9 (파랑) | 수락됨 |
| CONFIRMED | #76BCA2 (초록) | 확정됨 |
| IN_PROGRESS | #9B59B6 (보라) | 진행중 |
| COMPLETED | #888888 (회색) | 완료 |
| CANCELLED | #FF4444 (빨강) | 취소됨 |
| REJECTED | #FF4444 (빨강) | 거절됨 |

---

## 6. 예약 상세 UI (BookingDetailPage)

**경로:** `/bookings/:id`

**구성:**
- 예약 상태 배지 + 예약번호
- 시터 정보 섹션 (이름, 사진, 채팅하기 버튼)
- 서비스 정보 섹션 (유형, 날짜, 시간)
- 반려동물 정보 섹션 (이름, 품종, 나이)
- 결제 정보 섹션 (견적 내역, 총액)
- 요청사항 섹션
- 상태 이력 타임라인 (BookingStatusTimeline)
- 하단 버튼:
  - REQUESTED 상태: 취소하기
  - ACCEPTED 상태: 결제하기 / 취소하기
  - CONFIRMED 상태: 취소하기
  - COMPLETED 상태: 후기 작성 (7일 이내)

---

## 7. 시터 예약관리 UI (PartnerBookingListPage)

**경로:** `/partner/bookings`

### 7.1 탭 구성

| 탭 | 상태 필터 | 설명 |
|----|----------|------|
| 요청 | REQUESTED | 신규 예약 요청 |
| 확정 | ACCEPTED, CONFIRMED | 수락/결제 완료 건 |
| 진행중 | IN_PROGRESS | 돌봄 진행 중 |
| 완료 | COMPLETED | 완료 내역 |

### 7.2 PartnerBookingCard 구성

각 예약 카드에 포함되는 정보:
- 예약번호
- 반려인 이름 + 프로필 이미지
- 서비스 유형
- 날짜/시간
- 반려동물 이름 목록 + 마리 수
- 요청사항 미리보기
- 금액
- 상태 배지

**REQUESTED 상태 카드:**
- 수락 버튼 (Primary)
- 거절 버튼 (Secondary) → RejectReasonModal 오픈

**CONFIRMED 상태 카드:**
- 돌봄 시작 버튼

**IN_PROGRESS 상태 카드:**
- 돌봄 완료 버튼
- 돌봄 일지 작성 버튼

---

## 8. 타입 정의

```typescript
// types/booking.ts

export type ServiceType = 'DAY_CARE' | 'BOARDING' | 'WALKING' | 'GROOMING' | 'TRAINING';

export type BookingStatus =
  | 'REQUESTED'
  | 'ACCEPTED'
  | 'CONFIRMED'
  | 'IN_PROGRESS'
  | 'COMPLETED'
  | 'CANCELLED'
  | 'REJECTED';

export interface BookingPetInfo {
  petId: number;
  petName: string;
  petBreed: string;
  petAge?: number;
}

export interface BookingQuoteInfo {
  basePrice: number;
  additionalPetPrice: number;
  totalAmount: number;
  description: string;
}

export interface BookingStatusHistoryItem {
  fromStatus: BookingStatus | null;
  toStatus: BookingStatus;
  changedBy: string;
  reason: string | null;
  createdAt: string;
}

export interface BookingListItem {
  id: number;
  bookingNumber: string;
  partnerName: string;
  partnerProfileImageUrl: string;
  serviceType: ServiceType;
  startDate: string;
  endDate: string;
  status: BookingStatus;
  totalAmount: number;
  petNames: string[];
  createdAt: string;
}

export interface BookingDetail {
  id: number;
  bookingNumber: string;
  customerId: number;
  customerName: string;
  partnerId: number;
  partnerName: string;
  partnerProfileImageUrl: string;
  serviceType: ServiceType;
  startDate: string;
  endDate: string;
  status: BookingStatus;
  pets: BookingPetInfo[];
  quote: BookingQuoteInfo;
  totalAmount: number;
  requestNote: string | null;
  statusHistory: BookingStatusHistoryItem[];
  createdAt: string;
  updatedAt: string;
}

export interface PartnerBookingListItem {
  id: number;
  bookingNumber: string;
  customerName: string;
  customerProfileImageUrl: string;
  serviceType: ServiceType;
  startDate: string;
  endDate: string;
  status: BookingStatus;
  totalAmount: number;
  petNames: string[];
  petCount: number;
  requestNote: string | null;
  createdAt: string;
}

// 예약 생성 요청
export interface CreateBookingRequest {
  partnerId: number;
  serviceType: ServiceType;
  startDate: string;
  endDate: string;
  petIds: number[];
  requestNote?: string;
}

// 예약 취소 요청
export interface CancelBookingRequest {
  reason?: string;
}

// 예약 거절 요청
export interface RejectBookingRequest {
  reason: string;
}

// 취소 결과
export interface CancelBookingResult {
  id: number;
  bookingNumber: string;
  status: BookingStatus;
  cancelledAt: string;
  refundAmount: number;
  refundRate: number;
  message: string;
}

// 상태 변경 결과
export interface StatusChangeResult {
  id: number;
  bookingNumber: string;
  status: BookingStatus;
  reason?: string;
  message: string;
}
```

---

## 9. API 서비스

```typescript
// api/booking.ts

import { client } from './client';
import type {
  BookingListItem,
  BookingDetail,
  CreateBookingRequest,
  CancelBookingRequest,
  CancelBookingResult,
  RejectBookingRequest,
  StatusChangeResult,
  PartnerBookingListItem,
} from '../types/booking';

// === 반려인 API ===

export const createBooking = (data: CreateBookingRequest) =>
  client.post<BookingDetail>('/api/v1/bookings', data);

export const getMyBookings = (params: { status?: string; page?: number; size?: number }) =>
  client.get<{ content: BookingListItem[]; totalElements: number; totalPages: number; number: number }>(
    '/api/v1/bookings', { params }
  );

export const getBookingDetail = (id: number) =>
  client.get<BookingDetail>(`/api/v1/bookings/${id}`);

export const cancelBooking = (id: number, data: CancelBookingRequest) =>
  client.post<CancelBookingResult>(`/api/v1/bookings/${id}/cancel`, data);

// === 시터 API ===

export const getPartnerBookings = (params: { status?: string; page?: number; size?: number }) =>
  client.get<{ content: PartnerBookingListItem[]; totalElements: number; totalPages: number; number: number }>(
    '/api/v1/partner/bookings', { params }
  );

export const acceptBooking = (id: number) =>
  client.put<StatusChangeResult>(`/api/v1/partner/bookings/${id}/accept`);

export const rejectBooking = (id: number, data: RejectBookingRequest) =>
  client.put<StatusChangeResult>(`/api/v1/partner/bookings/${id}/reject`, data);

export const startBooking = (id: number) =>
  client.put<StatusChangeResult>(`/api/v1/partner/bookings/${id}/start`);

export const completeBooking = (id: number) =>
  client.put<StatusChangeResult>(`/api/v1/partner/bookings/${id}/complete`);
```

---

## 10. 커스텀 훅

```typescript
// hooks/useBooking.ts

import { useState, useEffect, useCallback } from 'react';
import * as bookingApi from '../api/booking';
import type { BookingListItem, BookingDetail, BookingStatus } from '../types/booking';

// 반려인: 예약 목록
export const useMyBookings = (status?: string) => {
  const [bookings, setBookings] = useState<BookingListItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const fetchBookings = useCallback(async () => {
    setLoading(true);
    try {
      const { data } = await bookingApi.getMyBookings({ status, page, size: 10 });
      setBookings(data.content);
      setTotalPages(data.totalPages);
    } finally {
      setLoading(false);
    }
  }, [status, page]);

  useEffect(() => { fetchBookings(); }, [fetchBookings]);

  return { bookings, loading, page, setPage, totalPages, refetch: fetchBookings };
};

// 반려인: 예약 상세
export const useBookingDetail = (id: number) => {
  const [booking, setBooking] = useState<BookingDetail | null>(null);
  const [loading, setLoading] = useState(false);

  const fetchDetail = useCallback(async () => {
    setLoading(true);
    try {
      const { data } = await bookingApi.getBookingDetail(id);
      setBooking(data);
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => { fetchDetail(); }, [fetchDetail]);

  return { booking, loading, refetch: fetchDetail };
};

// 시터: 예약 관리 목록
export const usePartnerBookings = (status?: string) => {
  // useMyBookings와 동일한 구조, bookingApi.getPartnerBookings 사용
};
```

---

## 11. 예약 4단계 상태 관리

예약 프로세스 진행 중 데이터를 유지하기 위해 Context 또는 상태 관리를 사용합니다.

```typescript
// context/BookingContext.tsx

interface BookingFormData {
  sitterId: number | null;
  sitterName: string;
  sitterProfileImageUrl: string;
  serviceType: ServiceType | null;
  startDate: string | null;
  endDate: string | null;
  selectedPetIds: number[];
  quote: BookingQuoteInfo | null;
  requestNote: string;
}

const initialState: BookingFormData = {
  sitterId: null,
  sitterName: '',
  sitterProfileImageUrl: '',
  serviceType: null,
  startDate: null,
  endDate: null,
  selectedPetIds: [],
  quote: null,
  requestNote: '',
};
```

> **참고:** Step 간 이동 시 브라우저 뒤로가기를 통한 데이터 유실 방지를 위해 sessionStorage 백업을 권장합니다.
