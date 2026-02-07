/**
 * @fileoverview Axios API 클라이언트 설정
 *
 * 백엔드 API와의 HTTP 통신을 담당하는 Axios 인스턴스를 설정합니다.
 * 요청/응답 인터셉터를 통해 인증 토큰 관리, 토큰 갱신, 에러 처리를 수행합니다.
 */

import axios, { AxiosError, AxiosInstance, InternalAxiosRequestConfig } from 'axios';
import { ApiResponse, ErrorDetail } from '../types/api';

/** API 베이스 URL (환경 변수에서 가져오거나 기본값 사용) */
const API_BASE_URL = process.env.REACT_APP_API_URL || '/api';

/**
 * Axios 인스턴스 생성
 * 기본 설정이 적용된 API 클라이언트입니다.
 */
const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  /** 요청 타임아웃: 30초 */
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

/**
 * 요청 인터셉터
 * 모든 요청에 저장된 Access Token을 Authorization 헤더에 추가합니다.
 */
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('accessToken');
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

/**
 * 응답 인터셉터
 *
 * 다음 기능을 수행합니다:
 * 1. 401 에러 시 Refresh Token으로 Access Token 갱신 시도
 * 2. 갱신 성공 시 원래 요청 재시도
 * 3. 갱신 실패 시 로그인 페이지로 리다이렉트
 * 4. 에러 응답을 표준 형식으로 변환
 */
apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError<ApiResponse<unknown>>) => {
    const originalRequest = error.config;

    // 401 에러 발생 시 토큰 갱신 시도
    if (error.response?.status === 401 && originalRequest) {
      const refreshToken = localStorage.getItem('refreshToken');

      if (refreshToken) {
        try {
          // Refresh Token으로 새 Access Token 요청
          const response = await axios.post<ApiResponse<{ accessToken: string; refreshToken: string }>>(
            `${API_BASE_URL}/v1/auth/refresh`,
            { refreshToken }
          );

          if (response.data.success && response.data.data) {
            const { accessToken, refreshToken: newRefreshToken } = response.data.data;
            localStorage.setItem('accessToken', accessToken);
            localStorage.setItem('refreshToken', newRefreshToken);

            // 새 토큰으로 원래 요청 재시도
            if (originalRequest.headers) {
              originalRequest.headers.Authorization = `Bearer ${accessToken}`;
            }
            return apiClient(originalRequest);
          }
        } catch (refreshError) {
          // 토큰 갱신 실패 시 로그인 페이지로 리다이렉트
          localStorage.removeItem('accessToken');
          localStorage.removeItem('refreshToken');
          const loginPath = window.location.pathname.startsWith('/admin') ? '/admin/login' : '/login';
          window.location.href = loginPath;
        }
      } else {
        // Refresh Token이 없으면 로그인 페이지로 리다이렉트
        const loginPath = window.location.pathname.startsWith('/admin') ? '/admin/login' : '/login';
        window.location.href = loginPath;
      }
    }

    // 에러 응답을 표준 ErrorDetail 형식으로 변환
    const errorDetail: ErrorDetail = error.response?.data?.error || {
      code: 'UNKNOWN_ERROR',
      message: error.message || '알 수 없는 오류가 발생했습니다.',
    };

    return Promise.reject(errorDetail);
  }
);

export default apiClient;
