/**
 * @fileoverview 관리자 인증 커스텀 훅
 *
 * 관리자 로그인/로그아웃 기능을 제공하는 React 훅입니다.
 * 로그인 성공 시 /admin/dashboard로, 로그아웃 시 /admin/login으로 이동합니다.
 * ADMIN/SUPER_ADMIN 역할만 로그인을 허용합니다.
 */

import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { authApi } from '../api/auth';
import { userApi } from '../api/user';
import { useAuthStore } from '../stores/authStore';
import { useUIStore } from '../stores/uiStore';
import { LoginRequest } from '../types/auth';

const ADMIN_ROLES = ['ADMIN', 'SUPER_ADMIN'];

export const useAdminAuth = () => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { login: setAuth, logout: clearAuth } = useAuthStore();
  const { addNotification } = useUIStore();

  const loginMutation = useMutation({
    mutationFn: authApi.login,
    onSuccess: async (data) => {
      localStorage.setItem('accessToken', data.accessToken);
      localStorage.setItem('refreshToken', data.refreshToken);

      const user = await userApi.getMe();

      if (!ADMIN_ROLES.includes(user.role)) {
        // 관리자 역할이 아닌 경우 토큰 제거 후 에러 표시
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        addNotification({ type: 'error', message: '관리자 권한이 없습니다.' });
        return;
      }

      setAuth(user, data.accessToken, data.refreshToken);
      addNotification({ type: 'success', message: '관리자 로그인에 성공했습니다.' });
      navigate('/admin/dashboard');
    },
    onError: (error: Error) => {
      addNotification({ type: 'error', message: error.message });
    },
  });

  const logout = () => {
    authApi.logout();
    clearAuth();
    queryClient.clear();
    addNotification({ type: 'info', message: '로그아웃되었습니다.' });
    navigate('/admin/login');
  };

  return {
    login: (data: LoginRequest) => loginMutation.mutate(data),
    logout,
    isLoginLoading: loginMutation.isPending,
  };
};
