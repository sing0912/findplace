/**
 * @fileoverview 반려동물 API 서비스
 */

import apiClient from './client';
import { ApiResponse } from '../types/api';
import {
  Pet,
  PetListResponse,
  CreatePetRequest,
  UpdatePetRequest,
  DeceasedRequest,
} from '../types/pet';

/**
 * 내 반려동물 목록 조회
 */
export const getMyPets = async (): Promise<PetListResponse> => {
  const response = await apiClient.get<ApiResponse<PetListResponse>>('/pets');
  return response.data.data!;
};

/**
 * 반려동물 상세 조회
 */
export const getPet = async (id: number): Promise<Pet> => {
  const response = await apiClient.get<ApiResponse<Pet>>(`/pets/${id}`);
  return response.data.data!;
};

/**
 * 반려동물 등록
 */
export const createPet = async (request: CreatePetRequest): Promise<Pet> => {
  const response = await apiClient.post<ApiResponse<Pet>>('/pets', request);
  return response.data.data!;
};

/**
 * 반려동물 정보 수정
 */
export const updatePet = async (id: number, request: UpdatePetRequest): Promise<Pet> => {
  const response = await apiClient.put<ApiResponse<Pet>>(`/pets/${id}`, request);
  return response.data.data!;
};

/**
 * 프로필 이미지 업로드
 */
export const uploadPetImage = async (id: number, file: File): Promise<Pet> => {
  const formData = new FormData();
  formData.append('file', file);

  const response = await apiClient.post<ApiResponse<Pet>>(`/pets/${id}/image`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
  return response.data.data!;
};

/**
 * 반려동물 삭제
 */
export const deletePet = async (id: number): Promise<void> => {
  await apiClient.delete(`/pets/${id}`);
};

/**
 * 사망 처리
 */
export const markAsDeceased = async (id: number, request: DeceasedRequest): Promise<Pet> => {
  const response = await apiClient.patch<ApiResponse<Pet>>(`/pets/${id}/deceased`, request);
  return response.data.data!;
};
