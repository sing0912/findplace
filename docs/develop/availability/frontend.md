# Availability 도메인 - 프론트엔드 지침

**최종 수정일:** 2026-02-07
**상태:** 확정
**Phase:** 2 (예약/결제)

---

## 1. 개요

Availability 도메인의 프론트엔드 화면 구현 영구 지침입니다. 시터의 월간 캘린더 관리 화면과 반려인의 시터 가용일 조회 화면을 포함합니다.

### 1.1 기능 목록

| # | 기능 | 사용자 | Figma |
|---|------|--------|-------|
| 1 | 시터 캘린더 관리 | 시터 | 구현 후 추가 |
| 2 | 시터 가용일 조회 | 반려인 | 구현 후 추가 |

---

## 2. 파일 구조

```
frontend/src/
├── components/
│   └── availability/
│       ├── MonthlyCalendar.tsx           # 월간 캘린더 (공통)
│       ├── CalendarDayCell.tsx           # 날짜 셀 (상태별 스타일)
│       ├── BlockDateModal.tsx            # 날짜 차단 모달 (기간 선택)
│       ├── BlockedDateCard.tsx           # 차단된 날짜 카드 (해제 버튼)
│       ├── BookedDateCard.tsx            # 예약 확정일 카드
│       ├── SitterAvailabilityCalendar.tsx # 반려인용 가용일 달력 (읽기전용)
│       └── index.ts
│
├── pages/
│   └── partner/
│       └── CalendarPage.tsx              # 시터 캘린더 관리 페이지
│
├── hooks/
│   └── useCalendar.ts                    # 캘린더 관련 커스텀 훅
│
└── api/
    └── availability.ts                   # 캘린더 API 서비스
```

---

## 3. 라우팅

| 경로 | 컴포넌트 | 인증 | 설명 |
|------|----------|------|------|
| /partner/calendar | CalendarPage | PARTNER | 시터 캘린더 관리 |

> **참고**: 반려인의 시터 가용일 조회는 별도 페이지가 아닌 시터 상세 페이지 또는 예약 Step 1에 `SitterAvailabilityCalendar` 컴포넌트로 삽입됩니다.

---

## 4. 시터 캘린더 페이지 (CalendarPage)

**경로:** `/partner/calendar`

### 4.1 레이아웃 구성

```
┌─────────────────────────────────────────────┐
│  ◀  2026년 2월  ▶                            │
│                                              │
│  일  월  화  수  목  금  토                    │
│  ─────────────────────────                   │
│  ..  ..  ..  ..  ..  ..  01                  │
│  02  03  04  05  06  07  08                  │
│  09 [10][11][12] 13  14 (15)                 │
│ (16)  17  18  19 [20]  21  22               │
│  23  24  25  26  27  28                      │
│                                              │
│  범례: [차단일]  (예약일)  일반                │
├─────────────────────────────────────────────┤
│  차단된 날짜                                  │
│  ┌──────────────────────────────────┐       │
│  │ 2/10 ~ 2/12  개인 일정    [해제] │       │
│  │ 2/20         -           [해제] │       │
│  └──────────────────────────────────┘       │
│                                              │
│  예약 확정 일정                                │
│  ┌──────────────────────────────────┐       │
│  │ 2/15~16  BK-00003  박반려  데이케어│       │
│  └──────────────────────────────────┘       │
│                                              │
│  [+ 날짜 차단하기]                            │
└─────────────────────────────────────────────┘
```

### 4.2 MonthlyCalendar 컴포넌트

**Props:**
- `year`: number
- `month`: number
- `blocks`: BlockInfo[] (차단 날짜 목록)
- `bookedDates`: BookedDateInfo[] (예약 확정 날짜 목록)
- `onDateClick`: (date: string) => void
- `onMonthChange`: (year: number, month: number) => void

### 4.3 CalendarDayCell 색상

| 상태 | 배경색 | 텍스트색 | 설명 |
|------|--------|----------|------|
| 일반 | #FFFFFF | #000000 | 예약 가능 |
| 차단 | #FFE0E0 (연빨강) | #FF4444 | 시터가 차단한 날짜 |
| 예약 확정 | #E0F0FF (연파랑) | #4A90D9 | 예약이 확정된 날짜 |
| 오늘 | 테두리 #76BCA2 | #000000 | 오늘 날짜 강조 |
| 과거 | #F5F5F5 (연회색) | #AAAAAA | 과거 날짜 (비활성) |

### 4.4 BlockDateModal

"날짜 차단하기" 버튼 또는 달력의 일반 날짜 클릭 시 오픈

**구성:**
- 시작일 선택 (DatePicker)
- 종료일 선택 (DatePicker) - 단일 날짜 시 시작일과 동일
- 사유 입력 (선택, 최대 200자)
- 취소 / 차단하기 버튼

### 4.5 상호작용

| 동작 | 결과 |
|------|------|
| 월 이동 (좌/우 화살표) | 해당 월 데이터 다시 조회 |
| 일반 날짜 클릭 | BlockDateModal 오픈 (클릭한 날짜 자동 선택) |
| 차단 날짜 클릭 | 차단 해제 확인 다이얼로그 |
| 예약 날짜 클릭 | 예약 상세 페이지로 이동 |
| 차단 카드 [해제] 클릭 | 차단 해제 확인 다이얼로그 |

---

## 5. 반려인 시터 가용일 조회 UI

### 5.1 SitterAvailabilityCalendar 컴포넌트

시터 상세 페이지의 요금 탭 하단 또는 예약 Step 1 페이지에 삽입되는 읽기 전용 캘린더입니다.

**Props:**
- `sitterId`: number
- `year`: number
- `month`: number
- `onDateSelect`: (date: string) => void (예약 Step 1에서 사용)
- `onMonthChange`: (year: number, month: number) => void

### 5.2 날짜 셀 색상 (반려인 뷰)

| 상태 | 배경색 | 텍스트색 | 설명 |
|------|--------|----------|------|
| 가용 | #FFFFFF | #000000 | 예약 가능 날짜 |
| 불가 | #F5F5F5 | #AAAAAA | 예약 불가 날짜 (차단 + 예약 확정 합산) |
| 선택됨 | #76BCA2 | #FFFFFF | 사용자가 선택한 날짜 |
| 오늘 | 테두리 #76BCA2 | #000000 | 오늘 날짜 |
| 과거 | #F5F5F5 | #CCCCCC | 과거 날짜 (선택 불가) |

---

## 6. 타입 정의

```typescript
// types/availability.ts

export interface CalendarBlockInfo {
  id: number;
  startDate: string;  // "YYYY-MM-DD"
  endDate: string;    // "YYYY-MM-DD"
  reason: string | null;
}

export interface BookedDateInfo {
  date: string;       // "YYYY-MM-DD"
  bookingId: number;
  bookingNumber: string;
  customerName: string;
  serviceType: string;
  status: string;
}

export interface MonthlyCalendarData {
  year: number;
  month: number;
  blocks: CalendarBlockInfo[];
  bookedDates: BookedDateInfo[];
}

export interface SitterAvailability {
  sitterId: number;
  sitterName: string;
  year: number;
  month: number;
  unavailableDates: string[];
  availableDates: string[];
}

// 차단 생성 요청
export interface CreateBlockRequest {
  startDate: string;  // "YYYY-MM-DD"
  endDate: string;    // "YYYY-MM-DD"
  reason?: string;
}
```

---

## 7. API 서비스

```typescript
// api/availability.ts

import { client } from './client';
import type {
  MonthlyCalendarData,
  SitterAvailability,
  CreateBlockRequest,
  CalendarBlockInfo,
} from '../types/availability';

// === 시터 API ===

export const getPartnerCalendar = (params: { year: number; month: number }) =>
  client.get<MonthlyCalendarData>('/api/v1/partner/calendar', { params });

export const createCalendarBlock = (data: CreateBlockRequest) =>
  client.post<CalendarBlockInfo>('/api/v1/partner/calendar/block', data);

export const deleteCalendarBlock = (id: number) =>
  client.delete<{ success: boolean; message: string }>(`/api/v1/partner/calendar/block/${id}`);

// === Public API ===

export const getSitterAvailability = (sitterId: number, params: { year: number; month: number }) =>
  client.get<SitterAvailability>(`/api/v1/sitters/${sitterId}/availability`, { params });
```

---

## 8. 커스텀 훅

```typescript
// hooks/useCalendar.ts

import { useState, useEffect, useCallback } from 'react';
import * as availabilityApi from '../api/availability';
import type { MonthlyCalendarData, SitterAvailability } from '../types/availability';

// 시터: 월간 캘린더
export const usePartnerCalendar = () => {
  const now = new Date();
  const [year, setYear] = useState(now.getFullYear());
  const [month, setMonth] = useState(now.getMonth() + 1);
  const [calendar, setCalendar] = useState<MonthlyCalendarData | null>(null);
  const [loading, setLoading] = useState(false);

  const fetchCalendar = useCallback(async () => {
    setLoading(true);
    try {
      const { data } = await availabilityApi.getPartnerCalendar({ year, month });
      setCalendar(data);
    } finally {
      setLoading(false);
    }
  }, [year, month]);

  useEffect(() => { fetchCalendar(); }, [fetchCalendar]);

  const goToPrevMonth = () => {
    if (month === 1) { setYear(y => y - 1); setMonth(12); }
    else { setMonth(m => m - 1); }
  };

  const goToNextMonth = () => {
    if (month === 12) { setYear(y => y + 1); setMonth(1); }
    else { setMonth(m => m + 1); }
  };

  return { year, month, calendar, loading, goToPrevMonth, goToNextMonth, refetch: fetchCalendar };
};

// 반려인: 시터 가용일 조회
export const useSitterAvailability = (sitterId: number) => {
  const now = new Date();
  const [year, setYear] = useState(now.getFullYear());
  const [month, setMonth] = useState(now.getMonth() + 1);
  const [availability, setAvailability] = useState<SitterAvailability | null>(null);
  const [loading, setLoading] = useState(false);

  const fetchAvailability = useCallback(async () => {
    setLoading(true);
    try {
      const { data } = await availabilityApi.getSitterAvailability(sitterId, { year, month });
      setAvailability(data);
    } finally {
      setLoading(false);
    }
  }, [sitterId, year, month]);

  useEffect(() => { fetchAvailability(); }, [fetchAvailability]);

  const goToPrevMonth = () => {
    if (month === 1) { setYear(y => y - 1); setMonth(12); }
    else { setMonth(m => m - 1); }
  };

  const goToNextMonth = () => {
    if (month === 12) { setYear(y => y + 1); setMonth(1); }
    else { setMonth(m => m + 1); }
  };

  return { year, month, availability, loading, goToPrevMonth, goToNextMonth, refetch: fetchAvailability };
};
```
