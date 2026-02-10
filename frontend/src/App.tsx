/**
 * @fileoverview 애플리케이션의 루트 컴포넌트
 *
 * 이 파일은 애플리케이션의 전체 구조를 정의하며, 다음을 포함합니다:
 * - React Query 클라이언트 설정 (서버 상태 관리)
 * - Material-UI 테마 설정 (다크 모드 지원)
 * - 라우팅 구성 (공개/보호/관리자 라우트)
 * - 전역 알림 시스템
 */

import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ThemeProvider, createTheme, CssBaseline } from '@mui/material';

// Layouts
import MainLayout from './components/layout/MainLayout';
import { AdminLayout } from './components/admin/layout';

// Pages
import HomePage from './pages/HomePage';
import PrivacyPolicyPage from './pages/legal/PrivacyPolicyPage';
import TermsOfUsePage from './pages/legal/TermsOfUsePage';
// Auth Pages
import {
  LoginStartPage,
  OAuthCallbackPage,
  RegisterStep1Page,
  RegisterStep2Page,
  RegisterCompletePage,
  FindIdPage,
  FindIdVerifyPage,
  FindIdResultPage,
  ResetPasswordPage,
  ResetPasswordVerifyPage,
  ResetPasswordConfirmPage,
} from './pages/auth';

// Admin Pages
import {
  AdminLoginPage,
  DashboardPage,
  UserManagementPage,
  PartnerManagementPage,
  PartnerReviewPage,
  ReservationManagementPage,
  DisputeManagementPage,
  SettlementPage,
  FeeSettingPage,
  InquiryManagementPage,
  FaqManagementPage,
  NoticeManagementPage,
  EventManagementPage,
  CommunityManagementPage,
  CampaignManagementPage,
  StatisticsPage,
  SettingsPage,
  AdminPolicyManagementPage,
  AdminProfilePage,
  AuditLogPage,
  AppVersionPage,
} from './pages/admin';

// MyPage Pages
import { MyPage, EditProfilePage, ChangePasswordPage, PolicyListPage } from './pages/mypage';

// Pet Pages
import {
  PetListPage,
  PetRegisterPage,
  PetEditPage,
  PetChecklistPage,
} from './pages/pet';

// Inquiry Pages
import {
  InquiryListPage,
  InquiryWritePage,
  InquiryDetailPage,
} from './pages/inquiry';

// Components
import ProtectedRoute from './components/common/ProtectedRoute';
import AdminProtectedRoute from './components/admin/common/AdminProtectedRoute';
import Notification from './components/common/Notification';

// Stores
import { useUIStore } from './stores/uiStore';

/**
 * React Query 클라이언트 인스턴스
 */
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      retry: 1,
      staleTime: 5 * 60 * 1000,
    },
  },
});

/**
 * Material-UI 테마 생성 함수
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
            textTransform: 'none',
          },
        },
      },
    },
  });

/**
 * 애플리케이션의 주요 콘텐츠 컴포넌트
 */
const AppContent: React.FC = () => {
  const { darkMode } = useUIStore();
  const theme = getTheme(darkMode);

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Notification />
      <BrowserRouter future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
        <Routes>
          {/* 공개 라우트 (사용자) - 인증 없이 접근 가능 */}
          <Route path="/login" element={<LoginStartPage />} />
          <Route path="/oauth/:provider/callback" element={<OAuthCallbackPage />} />

          {/* 회원가입 */}
          <Route path="/register" element={<RegisterStep1Page />} />
          <Route path="/register/info" element={<RegisterStep2Page />} />
          <Route path="/register/complete" element={<RegisterCompletePage />} />

          {/* 아이디 찾기 */}
          <Route path="/find-id" element={<FindIdPage />} />
          <Route path="/find-id/verify" element={<FindIdVerifyPage />} />
          <Route path="/find-id/result" element={<FindIdResultPage />} />

          {/* 비밀번호 재설정 */}
          <Route path="/reset-password" element={<ResetPasswordPage />} />
          <Route path="/reset-password/verify" element={<ResetPasswordVerifyPage />} />
          <Route path="/reset-password/confirm" element={<ResetPasswordConfirmPage />} />

          {/* 약관 */}
          <Route path="/privacy-policy" element={<PrivacyPolicyPage />} />
          <Route path="/privacy" element={<PrivacyPolicyPage />} />
          <Route path="/terms" element={<TermsOfUsePage />} />

          {/* 공개 라우트 (관리자) */}
          <Route path="/admin/login" element={<AdminLoginPage />} />

          {/* 관리자 보호 라우트 - AdminProtectedRoute + AdminLayout */}
          <Route
            path="/admin"
            element={
              <AdminProtectedRoute>
                <AdminLayout />
              </AdminProtectedRoute>
            }
          >
            <Route index element={<Navigate to="/admin/dashboard" replace />} />
            <Route path="dashboard" element={<DashboardPage />} />

            {/* 회원 관리 */}
            <Route path="members/users" element={<UserManagementPage />} />
            <Route path="members/partners" element={<PartnerManagementPage />} />
            <Route path="members/partner-review" element={<PartnerReviewPage />} />

            {/* 예약 관리 */}
            <Route path="reservations" element={<ReservationManagementPage />} />
            <Route path="reservations/disputes" element={<DisputeManagementPage />} />

            {/* 정산 관리 */}
            <Route path="settlement" element={<SettlementPage />} />
            <Route path="settlement/fees" element={<FeeSettingPage />} />

            {/* 고객센터 */}
            <Route path="cs/inquiries" element={<InquiryManagementPage />} />
            <Route path="cs/faq" element={<FaqManagementPage />} />

            {/* 콘텐츠 관리 */}
            <Route path="contents/notices" element={<NoticeManagementPage />} />
            <Route path="contents/events" element={<EventManagementPage />} />
            <Route path="contents/community" element={<CommunityManagementPage />} />
            <Route path="contents/campaigns" element={<CampaignManagementPage />} />

            {/* 통계 */}
            <Route path="statistics" element={<StatisticsPage />} />

            {/* 설정 */}
            <Route path="settings" element={<SettingsPage />} />
            <Route path="settings/policies" element={<AdminPolicyManagementPage />} />
            <Route path="settings/profile" element={<AdminProfilePage />} />
            <Route path="settings/audit-log" element={<AuditLogPage />} />
            <Route path="settings/app-version" element={<AppVersionPage />} />
          </Route>

          {/* 사용자 보호 라우트 - ProtectedRoute + MainLayout */}
          <Route
            path="/"
            element={
              <ProtectedRoute>
                <MainLayout />
              </ProtectedRoute>
            }
          >
            <Route index element={<HomePage />} />
            <Route path="search" element={<div>시터 검색 (준비 중)</div>} />
            <Route path="reservations" element={<div>예약 (준비 중)</div>} />
            <Route path="chat" element={<div>채팅 (준비 중)</div>} />

            {/* 마이페이지 */}
            <Route path="mypage" element={<MyPage />} />
            <Route path="mypage/edit" element={<EditProfilePage />} />
            <Route path="mypage/password" element={<ChangePasswordPage />} />
            <Route path="mypage/settings/policies" element={<PolicyListPage />} />

            {/* 펫 관리 */}
            <Route path="mypage/pets" element={<PetListPage />} />
            <Route path="mypage/pets/register" element={<PetRegisterPage />} />
            <Route path="mypage/pets/:id/edit" element={<PetEditPage />} />
            <Route path="mypage/pets/:id/checklist" element={<PetChecklistPage />} />

            {/* 문의 게시판 */}
            <Route path="mypage/inquiry" element={<InquiryListPage />} />
            <Route path="mypage/inquiry/write" element={<InquiryWritePage />} />
            <Route path="mypage/inquiry/:id" element={<InquiryDetailPage />} />
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
 */
const App: React.FC = () => {
  return (
    <QueryClientProvider client={queryClient}>
      <AppContent />
    </QueryClientProvider>
  );
};

export default App;
