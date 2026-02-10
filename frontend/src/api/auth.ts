/**
 * @fileoverview 인증 API 모듈
 *
 * 인증 관련 API 호출 함수들을 정의합니다.
 * 로그인, 회원가입, 토큰 갱신, 로그아웃 기능을 제공합니다.
 */

import apiClient from './client';
import { ApiResponse } from '../types/api';
import { LoginRequest, RegisterRequest, RegisterResult, TokenResponse, RefreshTokenRequest } from '../types/auth';

/**
 * 인증 API 객체
 * 인증 관련 모든 API 호출 함수를 포함합니다.
 */
export const authApi = {
  /**
   * 사용자 로그인을 수행합니다.
   * @param data - 로그인 요청 데이터 (이메일, 비밀번호)
   * @returns 액세스 토큰 및 리프레시 토큰
   * @throws 로그인 실패 시 에러
   */
  login: async (data: LoginRequest): Promise<TokenResponse> => {
    const response = await apiClient.post<ApiResponse<TokenResponse>>('/v1/auth/login', data);
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    throw new Error(response.data.error?.message || '로그인에 실패했습니다.');
  },

  /**
   * 새 사용자 계정을 생성합니다.
   * @param data - 회원가입 요청 데이터 (이메일, 비밀번호, 이름 등)
   * @returns 회원가입 결과 (id, email, nickname, createdAt)
   * @throws 회원가입 실패 시 에러
   */
  register: async (data: RegisterRequest): Promise<RegisterResult> => {
    const response = await apiClient.post<ApiResponse<RegisterResult>>('/v1/auth/register', data);
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    throw new Error(response.data.error?.message || '회원가입에 실패했습니다.');
  },

  /**
   * 만료된 액세스 토큰을 갱신합니다.
   * @param data - 토큰 갱신 요청 데이터 (리프레시 토큰)
   * @returns 새로운 액세스 토큰 및 리프레시 토큰
   * @throws 토큰 갱신 실패 시 에러
   */
  refresh: async (data: RefreshTokenRequest): Promise<TokenResponse> => {
    const response = await apiClient.post<ApiResponse<TokenResponse>>('/v1/auth/refresh', data);
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    throw new Error(response.data.error?.message || '토큰 갱신에 실패했습니다.');
  },

  /**
   * 로그아웃을 수행합니다.
   * localStorage에 저장된 토큰들을 제거합니다.
   * 서버에 별도의 요청을 보내지 않습니다 (클라이언트 사이드 로그아웃).
   */
  logout: (): void => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
  },
};
