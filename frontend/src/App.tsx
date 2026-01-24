/**
 * @fileoverview 애플리케이션의 루트 컴포넌트
 *
 * 이 파일은 애플리케이션의 전체 구조를 정의하며, 다음을 포함합니다:
 * - React Query 클라이언트 설정 (서버 상태 관리)
 * - Material-UI 테마 설정 (다크 모드 지원)
 * - 라우팅 구성 (공개/보호 라우트)
 * - 전역 알림 시스템
 */

import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ThemeProvider, createTheme, CssBaseline } from '@mui/material';

// Layouts
import MainLayout from './components/layout/MainLayout';

// Pages
import HomePage from './pages/HomePage';
import LoginPage from './pages/auth/LoginPage';
import RegisterPage from './pages/auth/RegisterPage';

// Components
import ProtectedRoute from './components/common/ProtectedRoute';
import Notification from './components/common/Notification';

// Stores
import { useUIStore } from './stores/uiStore';

/**
 * React Query 클라이언트 인스턴스
 * 서버 상태 관리를 위한 기본 옵션을 설정합니다.
 */
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      /** 윈도우 포커스 시 자동 재조회 비활성화 */
      refetchOnWindowFocus: false,
      /** 실패 시 1회 재시도 */
      retry: 1,
      /** 데이터 신선도 유지 시간: 5분 */
      staleTime: 5 * 60 * 1000,
    },
  },
});

/**
 * Material-UI 테마 생성 함수
 * @param darkMode - 다크 모드 활성화 여부
 * @returns 생성된 MUI 테마 객체
 */
const getTheme = (darkMode: boolean) =>
  createTheme({
    palette: {
      mode: darkMode ? 'dark' : 'light',
      primary: {
        main: '#1976d2',
      },
      secondary: {
        main: '#dc004e',
      },
    },
    typography: {
      fontFamily: [
        '-apple-system',
        'BlinkMacSystemFont',
        '"Segoe UI"',
        'Roboto',
        '"Helvetica Neue"',
        'Arial',
        'sans-serif',
      ].join(','),
    },
    components: {
      MuiButton: {
        styleOverrides: {
          root: {
            /** 버튼 텍스트 대문자 변환 비활성화 */
            textTransform: 'none',
          },
        },
      },
    },
  });

/**
 * 애플리케이션의 주요 콘텐츠 컴포넌트
 * 테마, 라우팅, 알림 시스템을 구성합니다.
 */
const AppContent: React.FC = () => {
  const { darkMode } = useUIStore();
  const theme = getTheme(darkMode);

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Notification />
      <BrowserRouter>
        <Routes>
          {/* 공개 라우트 - 인증 없이 접근 가능 */}
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />

          {/* 보호된 라우트 - 인증된 사용자만 접근 가능 */}
          <Route
            path="/"
            element={
              <ProtectedRoute>
                <MainLayout />
              </ProtectedRoute>
            }
          >
            <Route index element={<HomePage />} />
            <Route path="companies" element={<div>장례업체 목록</div>} />
            <Route path="products" element={<div>상품 목록</div>} />
            <Route path="reservations" element={<div>예약 관리</div>} />
            <Route path="orders" element={<div>주문 내역</div>} />
            <Route path="memorial" element={<div>추모관</div>} />

            {/* 관리자 전용 라우트 */}
            <Route
              path="admin/dashboard"
              element={
                <ProtectedRoute roles={['ADMIN', 'SUPER_ADMIN']}>
                  <div>관리자 대시보드</div>
                </ProtectedRoute>
              }
            />
            <Route
              path="admin/users"
              element={
                <ProtectedRoute roles={['ADMIN', 'SUPER_ADMIN']}>
                  <div>사용자 관리</div>
                </ProtectedRoute>
              }
            />
            <Route
              path="admin/companies"
              element={
                <ProtectedRoute roles={['ADMIN', 'SUPER_ADMIN']}>
                  <div>업체 관리</div>
                </ProtectedRoute>
              }
            />
            <Route
              path="admin/suppliers"
              element={
                <ProtectedRoute roles={['ADMIN', 'SUPER_ADMIN']}>
                  <div>공급사 관리</div>
                </ProtectedRoute>
              }
            />
          </Route>

          {/* 알 수 없는 경로는 홈으로 리다이렉트 */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </ThemeProvider>
  );
};

/**
 * 애플리케이션의 최상위 컴포넌트
 * React Query Provider로 전체 앱을 감싸서 서버 상태 관리 기능을 제공합니다.
 */
const App: React.FC = () => {
  return (
    <QueryClientProvider client={queryClient}>
      <AppContent />
    </QueryClientProvider>
  );
};

export default App;
