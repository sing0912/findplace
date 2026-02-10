/**
 * @fileoverview 인증 커스텀 훅
 *
 * 인증 관련 기능을 제공하는 React 훅입니다.
 * 로그인, 회원가입, 로그아웃, 현재 사용자 조회 등의 기능을 포함합니다.
 * React Query를 사용하여 서버 상태를 관리합니다.
 */

import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { authApi } from '../api/auth';
import { userApi } from '../api/user';
import { useAuthStore } from '../stores/authStore';
import { useUIStore } from '../stores/uiStore';
import { LoginRequest, RegisterRequest } from '../types/auth';

/**
 * 인증 커스텀 훅
 *
 * 인증 관련 모든 기능을 제공하는 훅입니다.
 *
 * 제공 기능:
 * - 현재 사용자 조회 (자동)
 * - 로그인/회원가입 처리
 * - 로그아웃 처리
 * - 로딩 상태 제공
 *
 * @returns 인증 관련 상태와 함수들
 *
 * @example
 * const { login, logout, isLoginLoading, currentUser } = useAuth();
 *
 * const handleLogin = () => {
 *   login({ email: 'user@example.com', password: 'password' });
 * };
 */
export const useAuth = () => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { login: setAuth, logout: clearAuth } = useAuthStore();
  const { addNotification } = useUIStore();

  /**
   * 현재 로그인한 사용자 정보 조회
   * accessToken이 있을 때만 자동으로 실행됩니다.
   */
  const { data: currentUser, isLoading: isUserLoading } = useQuery({
    queryKey: ['currentUser'],
    queryFn: userApi.getMe,
    enabled: !!localStorage.getItem('accessToken'),
    retry: false,
    staleTime: 5 * 60 * 1000, // 5분간 캐시 유지
  });

  /**
   * 로그인 뮤테이션
   * 성공 시 토큰을 저장하고 홈 페이지로 이동합니다.
   */
  const loginMutation = useMutation({
    mutationFn: authApi.login,
    onSuccess: async (data) => {
      localStorage.setItem('accessToken', data.accessToken);
      localStorage.setItem('refreshToken', data.refreshToken);

      const user = await userApi.getMe();
      setAuth(user, data.accessToken, data.refreshToken);

      addNotification({ type: 'success', message: '로그인에 성공했습니다.' });
      navigate('/');
    },
    onError: (error: Error) => {
      addNotification({ type: 'error', message: error.message });
    },
  });

  /**
   * 회원가입 뮤테이션
   * 성공 시 자동 로그인되어 홈 페이지로 이동합니다.
   */
  const registerMutation = useMutation({
    mutationFn: authApi.register,
    onSuccess: () => {
      addNotification({ type: 'success', message: '회원가입에 성공했습니다.' });
      navigate('/register/complete');
    },
    onError: (error: Error) => {
      addNotification({ type: 'error', message: error.message });
    },
  });

  /**
   * 로그아웃 처리 함수
   * 토큰을 제거하고 캐시를 초기화한 후 로그인 페이지로 이동합니다.
   */
  const logout = () => {
    authApi.logout();
    clearAuth();
    queryClient.clear();
    addNotification({ type: 'info', message: '로그아웃되었습니다.' });
    navigate('/login');
  };

  return {
    /** 현재 로그인한 사용자 정보 */
    currentUser,
    /** 사용자 정보 로딩 중 여부 */
    isLoading: isUserLoading,
    /**
     * 로그인 함수
     * @param data - 로그인 요청 데이터 (이메일, 비밀번호)
     */
    login: (data: LoginRequest) => loginMutation.mutate(data),
    /**
     * 회원가입 함수
     * @param data - 회원가입 요청 데이터 (이메일, 비밀번호, 이름 등)
     */
    register: (data: RegisterRequest) => registerMutation.mutate(data),
    /** 로그아웃 함수 */
    logout,
    /** 로그인 처리 중 여부 */
    isLoginLoading: loginMutation.isPending,
    /** 회원가입 처리 중 여부 */
    isRegisterLoading: registerMutation.isPending,
  };
};
