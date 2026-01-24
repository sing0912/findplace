/**
 * @fileoverview 보호된 라우트 컴포넌트
 *
 * 인증이 필요한 라우트를 보호하는 래퍼 컴포넌트입니다.
 * 인증되지 않은 사용자는 로그인 페이지로 리다이렉트되며,
 * 권한이 없는 사용자는 홈 페이지로 리다이렉트됩니다.
 */

import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { Box, CircularProgress } from '@mui/material';
import { useAuthStore } from '../../stores/authStore';

/**
 * ProtectedRoute 컴포넌트 Props 인터페이스
 */
interface ProtectedRouteProps {
  /** 보호할 자식 컴포넌트 */
  children: React.ReactNode;
  /** 접근 가능한 역할 목록 (미지정 시 인증된 모든 사용자 접근 가능) */
  roles?: string[];
}

/**
 * 보호된 라우트 컴포넌트
 *
 * 인증 및 권한 검사를 수행하여 라우트 접근을 제어합니다.
 *
 * 동작 방식:
 * 1. 로딩 중: 스피너 표시
 * 2. 미인증: 로그인 페이지로 리다이렉트 (현재 위치 저장)
 * 3. 권한 없음: 홈 페이지로 리다이렉트
 * 4. 인증 및 권한 확인: 자식 컴포넌트 렌더링
 *
 * @example
 * // 인증만 필요한 경우
 * <ProtectedRoute>
 *   <Dashboard />
 * </ProtectedRoute>
 *
 * @example
 * // 특정 역할이 필요한 경우
 * <ProtectedRoute roles={['ADMIN', 'SUPER_ADMIN']}>
 *   <AdminPanel />
 * </ProtectedRoute>
 */
const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children, roles }) => {
  const { user, isAuthenticated, isLoading } = useAuthStore();
  const location = useLocation();

  // 인증 상태 확인 중 로딩 스피너 표시
  if (isLoading) {
    return (
      <Box
        sx={{
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          minHeight: '100vh',
        }}
      >
        <CircularProgress />
      </Box>
    );
  }

  // 인증되지 않은 경우 로그인 페이지로 리다이렉트
  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  // 역할 기반 권한 검사: 필요한 역할이 없는 경우 홈으로 리다이렉트
  if (roles && user && !roles.includes(user.role)) {
    return <Navigate to="/" replace />;
  }

  // 인증 및 권한 확인 완료: 자식 컴포넌트 렌더링
  return <>{children}</>;
};

export default ProtectedRoute;
