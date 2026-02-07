/**
 * @fileoverview 사용자 API 모듈
 *
 * 사용자 관련 API 호출 함수들을 정의합니다.
 * 사용자 CRUD 작업, 프로필 조회, 비밀번호 변경 등의 기능을 제공합니다.
 */

import apiClient from './client';
import { ApiResponse, PageResponse, PageRequest } from '../types/api';
import { User, UserSimple, CreateUserRequest, UpdateUserRequest, UpdatePasswordRequest } from '../types/user';

/**
 * 사용자 API 객체
 * 사용자 관련 모든 API 호출 함수를 포함합니다.
 */
export const userApi = {
  /**
   * 새 사용자를 생성합니다. (관리자 기능)
   * @param data - 생성할 사용자 정보
   * @returns 생성된 사용자 정보
   * @throws 사용자 생성 실패 시 에러
   */
  create: async (data: CreateUserRequest): Promise<User> => {
    const response = await apiClient.post<ApiResponse<User>>('/v1/users', data);
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    throw new Error(response.data.error?.message || '사용자 생성에 실패했습니다.');
  },

  /**
   * ID로 특정 사용자를 조회합니다.
   * @param id - 조회할 사용자 ID
   * @returns 사용자 상세 정보
   * @throws 사용자 조회 실패 시 에러
   */
  getById: async (id: number): Promise<User> => {
    const response = await apiClient.get<ApiResponse<User>>(`/v1/users/${id}`);
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    throw new Error(response.data.error?.message || '사용자 조회에 실패했습니다.');
  },

  /**
   * 현재 로그인한 사용자의 정보를 조회합니다.
   * @returns 현재 사용자 정보
   * @throws 내 정보 조회 실패 시 에러
   */
  getMe: async (): Promise<User> => {
    const response = await apiClient.get<ApiResponse<User>>('/v1/users/me');
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    throw new Error(response.data.error?.message || '내 정보 조회에 실패했습니다.');
  },

  /**
   * 사용자 목록을 페이지네이션하여 조회합니다. (관리자 기능)
   * @param params - 페이지네이션 파라미터 (페이지 번호, 크기, 정렬)
   * @returns 페이지네이션된 사용자 목록
   * @throws 목록 조회 실패 시 에러
   */
  getList: async (params?: PageRequest): Promise<PageResponse<UserSimple>> => {
    const response = await apiClient.get<ApiResponse<PageResponse<UserSimple>>>('/v1/users', { params });
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    throw new Error(response.data.error?.message || '사용자 목록 조회에 실패했습니다.');
  },

  /**
   * 키워드로 사용자를 검색합니다. (관리자 기능)
   * @param keyword - 검색할 키워드 (이름, 이메일 등)
   * @param params - 페이지네이션 파라미터
   * @returns 검색 결과 (페이지네이션)
   * @throws 검색 실패 시 에러
   */
  search: async (keyword: string, params?: PageRequest): Promise<PageResponse<UserSimple>> => {
    const response = await apiClient.get<ApiResponse<PageResponse<UserSimple>>>('/v1/users/search', {
      params: { keyword, ...params },
    });
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    throw new Error(response.data.error?.message || '사용자 검색에 실패했습니다.');
  },

  /**
   * 사용자 정보를 수정합니다.
   * @param id - 수정할 사용자 ID
   * @param data - 수정할 정보 (이름, 전화번호, 프로필 이미지 등)
   * @returns 수정된 사용자 정보
   * @throws 수정 실패 시 에러
   */
  update: async (id: number, data: UpdateUserRequest): Promise<User> => {
    const response = await apiClient.put<ApiResponse<User>>(`/v1/users/${id}`, data);
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    throw new Error(response.data.error?.message || '사용자 수정에 실패했습니다.');
  },

  /**
   * 사용자의 비밀번호를 변경합니다.
   * @param id - 비밀번호를 변경할 사용자 ID
   * @param data - 현재 비밀번호와 새 비밀번호
   * @throws 비밀번호 변경 실패 시 에러
   */
  updatePassword: async (id: number, data: UpdatePasswordRequest): Promise<void> => {
    const response = await apiClient.put<ApiResponse<void>>(`/v1/users/${id}/password`, data);
    if (!response.data.success) {
      throw new Error(response.data.error?.message || '비밀번호 변경에 실패했습니다.');
    }
  },

  /**
   * 사용자를 삭제합니다. (관리자 기능)
   * @param id - 삭제할 사용자 ID
   * @throws 삭제 실패 시 에러
   */
  delete: async (id: number): Promise<void> => {
    const response = await apiClient.delete<ApiResponse<void>>(`/v1/users/${id}`);
    if (!response.data.success) {
      throw new Error(response.data.error?.message || '사용자 삭제에 실패했습니다.');
    }
  },
};
