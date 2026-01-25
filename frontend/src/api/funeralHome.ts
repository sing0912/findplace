/**
 * @fileoverview 장례식장 API 서비스
 */

import apiClient from './client';
import type {
  FuneralHomeListItem,
  FuneralHomeDetail,
  NearbyFuneralHomesResult,
  NearbySearchRequest,
  FuneralHomeListRequest,
  SyncLogItem,
  SyncResult,
} from '../types/funeralHome';
import type { ApiResponse, PageResponse } from '../types/api';

const BASE_URL = '/v1/funeral-homes';
const ADMIN_BASE_URL = '/v1/admin/funeral-homes';

/**
 * 근처 장례식장 검색
 */
export const searchNearbyFuneralHomes = async (
  request: NearbySearchRequest
): Promise<NearbyFuneralHomesResult> => {
  const params = new URLSearchParams();
  params.append('latitude', request.latitude.toString());
  params.append('longitude', request.longitude.toString());
  if (request.radius) params.append('radius', request.radius.toString());
  if (request.limit) params.append('limit', request.limit.toString());
  if (request.hasCrematorium !== undefined) {
    params.append('hasCrematorium', request.hasCrematorium.toString());
  }
  if (request.hasFuneral !== undefined) {
    params.append('hasFuneral', request.hasFuneral.toString());
  }
  if (request.hasColumbarium !== undefined) {
    params.append('hasColumbarium', request.hasColumbarium.toString());
  }

  const response = await apiClient.get<ApiResponse<NearbyFuneralHomesResult>>(
    `${BASE_URL}/nearby?${params.toString()}`
  );
  return response.data.data!;
};

/**
 * 장례식장 상세 조회
 */
export const getFuneralHomeById = async (id: number): Promise<FuneralHomeDetail> => {
  const response = await apiClient.get<ApiResponse<FuneralHomeDetail>>(`${BASE_URL}/${id}`);
  return response.data.data!;
};

/**
 * 장례식장 목록 조회
 */
export const getFuneralHomes = async (
  request: FuneralHomeListRequest
): Promise<PageResponse<FuneralHomeListItem>> => {
  const params = new URLSearchParams();
  if (request.keyword) params.append('keyword', request.keyword);
  if (request.locCode) params.append('locCode', request.locCode);
  if (request.hasCrematorium !== undefined) {
    params.append('hasCrematorium', request.hasCrematorium.toString());
  }
  if (request.hasFuneral !== undefined) {
    params.append('hasFuneral', request.hasFuneral.toString());
  }
  if (request.hasColumbarium !== undefined) {
    params.append('hasColumbarium', request.hasColumbarium.toString());
  }
  if (request.page !== undefined) params.append('page', request.page.toString());
  if (request.size !== undefined) params.append('size', request.size.toString());

  const response = await apiClient.get<ApiResponse<PageResponse<FuneralHomeListItem>>>(
    `${BASE_URL}?${params.toString()}`
  );
  return response.data.data!;
};

// ========== 관리자 API ==========

/**
 * 장례식장 상태 변경 (관리자)
 */
export const updateFuneralHomeStatus = async (
  id: number,
  isActive: boolean
): Promise<void> => {
  await apiClient.patch(`${ADMIN_BASE_URL}/${id}/status`, { isActive });
};

/**
 * 증분 동기화 실행 (관리자)
 */
export const runIncrementalSync = async (): Promise<SyncResult> => {
  const response = await apiClient.post<ApiResponse<SyncResult>>(`${ADMIN_BASE_URL}/sync/incremental`);
  return response.data.data!;
};

/**
 * 전체 동기화 실행 (관리자)
 */
export const runFullSync = async (): Promise<SyncResult> => {
  const response = await apiClient.post<ApiResponse<SyncResult>>(`${ADMIN_BASE_URL}/sync/full`);
  return response.data.data!;
};

/**
 * 동기화 로그 조회 (관리자)
 */
export const getSyncLogs = async (
  page: number = 0,
  size: number = 20
): Promise<PageResponse<SyncLogItem>> => {
  const response = await apiClient.get<ApiResponse<PageResponse<SyncLogItem>>>(
    `${ADMIN_BASE_URL}/sync/logs?page=${page}&size=${size}`
  );
  return response.data.data!;
};

/**
 * 캐시 삭제 (관리자)
 */
export const evictCache = async (): Promise<void> => {
  await apiClient.post(`${ADMIN_BASE_URL}/cache/evict`);
};
