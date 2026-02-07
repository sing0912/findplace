/**
 * @fileoverview 관리자 보호 라우트 컴포넌트
 *
 * 관리자 권한이 필요한 라우트를 보호하는 래퍼 컴포넌트입니다.
 * 미인증 또는 권한 없는 사용자는 관리자 로그인 페이지로 리다이렉트됩니다.
 */

import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { Box, CircularProgress } from '@mui/material';
import { useAuthStore } from '../../../stores/authStore';

interface AdminProtectedRouteProps {
  children: React.ReactNode;
  roles?: string[];
}

/**
 * 관리자 보호 라우트 컴포넌트
 *
 * 동작 방식:
 * 1. 로딩 중: 스피너 표시
 * 2. 미인증: /admin/login으로 리다이렉트 (현재 위치 저장)
 * 3. 역할 불일치: /admin/login으로 리다이렉트
 * 4. 인증 + 권한 확인: children 렌더링
 */
const AdminProtectedRoute: React.FC<AdminProtectedRouteProps> = ({
  children,
  roles = ['ADMIN', 'SUPER_ADMIN'],
}) => {
  const { user, isAuthenticated, isLoading } = useAuthStore();
  const location = useLocation();

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

  if (!isAuthenticated) {
    return <Navigate to="/admin/login" state={{ from: location }} replace />;
  }

  if (user && !roles.includes(user.role)) {
    return <Navigate to="/admin/login" replace />;
  }

  return <>{children}</>;
};

export default AdminProtectedRoute;
