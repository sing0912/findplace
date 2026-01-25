/**
 * @fileoverview 지역 관련 타입 정의
 */

/** 지역 유형 */
export type RegionType = 'METRO' | 'CITY';

/** 지역 기본 정보 */
export interface Region {
  id: number;
  code: string;
  name: string;
  type: RegionType;
  parentCode: string | null;
  sortOrder: number;
  isActive: boolean;
}

/** 광역시/도 정보 */
export interface Metro {
  code: string;
  name: string;
  cityCount: number;
}

/** 광역시/도 목록 응답 */
export interface MetroListResponse {
  metros: Metro[];
  totalCount: number;
}

/** 시/군/구 정보 */
export interface City {
  code: string;
  name: string;
}

/** 시/군/구 목록 응답 */
export interface CityListResponse {
  metroCode: string;
  metroName: string;
  cities: City[];
  totalCount: number;
}

/** 계층 구조 지역 정보 */
export interface RegionHierarchy {
  code: string;
  name: string;
  type: RegionType;
  children: RegionHierarchy[];
}

/** 계층 구조 목록 응답 */
export interface RegionHierarchyResponse {
  regions: RegionHierarchy[];
}
