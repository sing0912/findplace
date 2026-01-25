/**
 * @fileoverview 반려동물 관련 타입 정의
 */

/** 반려동물 종류 */
export type Species = 'DOG' | 'CAT' | 'BIRD' | 'HAMSTER' | 'RABBIT' | 'FISH' | 'REPTILE' | 'ETC';

/** 반려동물 성별 */
export type Gender = 'MALE' | 'FEMALE' | 'UNKNOWN';

/** 종류별 한글명 */
export const SPECIES_NAMES: Record<Species, string> = {
  DOG: '강아지',
  CAT: '고양이',
  BIRD: '새',
  HAMSTER: '햄스터',
  RABBIT: '토끼',
  FISH: '물고기',
  REPTILE: '파충류',
  ETC: '기타',
};

/** 성별별 한글명 */
export const GENDER_NAMES: Record<Gender, string> = {
  MALE: '수컷',
  FEMALE: '암컷',
  UNKNOWN: '모름',
};

/** 반려동물 상세 정보 */
export interface Pet {
  id: number;
  userId: number;
  name: string;
  species: Species;
  speciesName: string;
  breed: string | null;
  birthDate: string | null;
  age: number | null;
  gender: Gender | null;
  genderName: string | null;
  isNeutered: boolean;
  profileImageUrl: string | null;
  memo: string | null;
  isDeceased: boolean;
  deceasedAt: string | null;
  createdAt: string;
  updatedAt: string;
}

/** 반려동물 요약 정보 */
export interface PetSummary {
  id: number;
  name: string;
  species: Species;
  speciesName: string;
  breed: string | null;
  age: number | null;
  gender: Gender | null;
  isDeceased: boolean;
  deceasedAt: string | null;
  profileImageUrl: string | null;
}

/** 반려동물 목록 응답 */
export interface PetListResponse {
  content: PetSummary[];
  totalCount: number;
  aliveCount: number;
  deceasedCount: number;
}

/** 반려동물 등록 요청 */
export interface CreatePetRequest {
  name: string;
  species: Species;
  breed?: string;
  birthDate?: string;
  gender?: Gender;
  isNeutered?: boolean;
  memo?: string;
}

/** 반려동물 수정 요청 */
export interface UpdatePetRequest {
  name: string;
  species: Species;
  breed?: string;
  birthDate?: string;
  gender?: Gender;
  isNeutered?: boolean;
  memo?: string;
}

/** 사망 처리 요청 */
export interface DeceasedRequest {
  deceasedAt: string;
}
