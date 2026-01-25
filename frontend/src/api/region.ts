/**
 * @fileoverview 지역 API 서비스
 */

import apiClient from './client';
import { ApiResponse } from '../types/api';
import {
  Region,
  MetroListResponse,
  CityListResponse,
  RegionHierarchyResponse,
} from '../types/region';

/**
 * 광역시/도 목록 조회
 */
export const getMetros = async (): Promise<MetroListResponse> => {
  const response = await apiClient.get<ApiResponse<MetroListResponse>>('/regions/metros');
  return response.data.data!;
};

/**
 * 특정 광역시/도의 시/군/구 목록 조회
 */
export const getCities = async (metroCode: string): Promise<CityListResponse> => {
  const response = await apiClient.get<ApiResponse<CityListResponse>>(
    `/regions/${metroCode}/cities`
  );
  return response.data.data!;
};

/**
 * 지역 코드로 상세 조회
 */
export const getRegionByCode = async (code: string): Promise<Region> => {
  const response = await apiClient.get<ApiResponse<Region>>(`/regions/${code}`);
  return response.data.data!;
};

/**
 * 전체 지역을 계층 구조로 조회
 */
export const getRegionHierarchy = async (): Promise<RegionHierarchyResponse> => {
  const response = await apiClient.get<ApiResponse<RegionHierarchyResponse>>('/regions/hierarchy');
  return response.data.data!;
};

/**
 * 전체 활성 지역 목록 조회
 */
export const getAllRegions = async (): Promise<Region[]> => {
  const response = await apiClient.get<ApiResponse<Region[]>>('/regions');
  return response.data.data!;
};
