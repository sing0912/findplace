/**
 * @fileoverview 반려동물 성향 체크리스트 타입 정의
 * @see docs/develop/pet/frontend.md
 */

/** 성향 체크리스트 */
export interface PetChecklist {
  id: number;
  petId: number;
  friendlyToStrangers: number;
  friendlyToDogs: number;
  friendlyToCats: number;
  activityLevel: number;
  barkingLevel: number;
  separationAnxiety: number;
  houseTraining: number;
  commandTraining: number;
  eatingHabit: string | null;
  walkPreference: string | null;
  fearItems: string | null;
  additionalNotes: string | null;
  createdAt: string;
  updatedAt: string;
}

/** 성향 체크리스트 생성 요청 */
export interface CreatePetChecklistRequest {
  friendlyToStrangers: number;
  friendlyToDogs: number;
  friendlyToCats: number;
  activityLevel: number;
  barkingLevel: number;
  separationAnxiety: number;
  houseTraining: number;
  commandTraining: number;
  eatingHabit?: string;
  walkPreference?: string;
  fearItems?: string;
  additionalNotes?: string;
}

/** 성향 체크리스트 수정 요청 */
export interface UpdatePetChecklistRequest {
  friendlyToStrangers: number;
  friendlyToDogs: number;
  friendlyToCats: number;
  activityLevel: number;
  barkingLevel: number;
  separationAnxiety: number;
  houseTraining: number;
  commandTraining: number;
  eatingHabit?: string;
  walkPreference?: string;
  fearItems?: string;
  additionalNotes?: string;
}

/** 식사 습관 옵션 */
export const EATING_HABIT_OPTIONS = [
  { value: '잘 먹음', label: '잘 먹음' },
  { value: '편식', label: '편식' },
  { value: '소식', label: '소식' },
] as const;

/** 산책 선호도 옵션 */
export const WALK_PREFERENCE_OPTIONS = [
  { value: '좋아함', label: '좋아함' },
  { value: '보통', label: '보통' },
  { value: '싫어함', label: '싫어함' },
] as const;

/** 수치형 항목 라벨 */
export const CHECKLIST_SLIDER_LABELS: Record<string, string> = {
  friendlyToStrangers: '낯선 사람 친화도',
  friendlyToDogs: '다른 강아지 친화도',
  friendlyToCats: '고양이 친화도',
  activityLevel: '활동량',
  barkingLevel: '짖음 정도',
  separationAnxiety: '분리불안',
  houseTraining: '배변 훈련',
  commandTraining: '명령어 훈련',
};
