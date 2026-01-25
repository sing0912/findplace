/**
 * @fileoverview 장례식장 타입 정의
 */

/**
 * 장례식장 목록 항목
 */
export interface FuneralHomeListItem {
  id: number;
  name: string;
  roadAddress: string;
  phone: string;
  locName: string;
  hasCrematorium: boolean;
  hasColumbarium: boolean;
  hasFuneral: boolean;
  latitude: number | null;
  longitude: number | null;
  distance?: number;
}

/**
 * 장례식장 상세 정보
 */
export interface FuneralHomeDetail {
  id: number;
  name: string;
  roadAddress: string;
  lotAddress: string;
  phone: string;
  locCode: string;
  locName: string;
  services: {
    hasCrematorium: boolean;
    hasColumbarium: boolean;
    hasFuneral: boolean;
  };
  location: {
    latitude: number | null;
    longitude: number | null;
  };
  isActive: boolean;
  syncedAt: string;
  createdAt: string;
}

/**
 * 근처 장례식장 검색 결과
 */
export interface NearbyFuneralHomesResult {
  content: FuneralHomeListItem[];
  totalCount: number;
  radius: number;
}

/**
 * 근처 장례식장 검색 요청
 */
export interface NearbySearchRequest {
  latitude: number;
  longitude: number;
  radius?: number;
  limit?: number;
  hasCrematorium?: boolean;
  hasFuneral?: boolean;
  hasColumbarium?: boolean;
}

/**
 * 장례식장 목록 검색 요청
 */
export interface FuneralHomeListRequest {
  keyword?: string;
  locCode?: string;
  hasCrematorium?: boolean;
  hasFuneral?: boolean;
  hasColumbarium?: boolean;
  page?: number;
  size?: number;
}

/**
 * 동기화 유형
 */
export type SyncType = 'INCREMENTAL' | 'FULL';

/**
 * 동기화 상태
 */
export type SyncStatus = 'RUNNING' | 'COMPLETED' | 'FAILED' | 'PARTIAL';

/**
 * 동기화 로그 항목
 */
export interface SyncLogItem {
  id: number;
  syncType: SyncType;
  startedAt: string;
  completedAt: string | null;
  status: SyncStatus;
  totalCount: number;
  insertedCount: number;
  updatedCount: number;
  deletedCount: number;
  errorCount: number;
  errorMessage: string | null;
}

/**
 * 동기화 실행 결과
 */
export interface SyncResult {
  logId: number;
  syncType: SyncType;
  status: SyncStatus;
  totalCount: number;
  insertedCount: number;
  updatedCount: number;
  deletedCount: number;
  message: string;
}
