/**
 * @fileoverview 반려동물 성향 체크리스트 API 서비스
 * @see docs/develop/pet/frontend.md
 */

import apiClient from './client';
import { ApiResponse } from '../types/api';
import {
  PetChecklist,
  CreatePetChecklistRequest,
  UpdatePetChecklistRequest,
} from '../types/petChecklist';

/**
 * 성향 체크리스트 조회
 */
export const getChecklist = async (petId: number): Promise<PetChecklist> => {
  const response = await apiClient.get<ApiResponse<PetChecklist>>(`/v1/pets/${petId}/checklist`);
  return response.data.data!;
};

/**
 * 성향 체크리스트 생성
 */
export const createChecklist = async (
  petId: number,
  request: CreatePetChecklistRequest
): Promise<PetChecklist> => {
  const response = await apiClient.post<ApiResponse<PetChecklist>>(
    `/v1/pets/${petId}/checklist`,
    request
  );
  return response.data.data!;
};

/**
 * 성향 체크리스트 수정
 */
export const updateChecklist = async (
  petId: number,
  request: UpdatePetChecklistRequest
): Promise<PetChecklist> => {
  const response = await apiClient.put<ApiResponse<PetChecklist>>(
    `/v1/pets/${petId}/checklist`,
    request
  );
  return response.data.data!;
};
