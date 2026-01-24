/**
 * @fileoverview 메인 레이아웃 컴포넌트
 *
 * 인증된 사용자를 위한 주요 레이아웃 구조를 정의합니다.
 * Header, Sidebar, 그리고 메인 콘텐츠 영역으로 구성됩니다.
 * 중첩 라우팅을 위해 Outlet을 사용합니다.
 */

import React from 'react';
import { Outlet } from 'react-router-dom';
import { Box, Toolbar } from '@mui/material';
import Header from './Header';
import Sidebar from './Sidebar';
import { useUIStore } from '../../stores/uiStore';

/** 사이드바 너비 (픽셀) */
const DRAWER_WIDTH = 240;

/**
 * 메인 레이아웃 컴포넌트
 *
 * 애플리케이션의 기본 레이아웃을 제공합니다.
 * - 상단: Header (앱바)
 * - 좌측: Sidebar (네비게이션 드로어)
 * - 중앙: 메인 콘텐츠 영역 (Outlet으로 자식 라우트 렌더링)
 *
 * 사이드바의 열림/닫힘 상태에 따라 메인 콘텐츠 영역의 너비가 조절됩니다.
 */
const MainLayout: React.FC = () => {
  const { sidebarOpen } = useUIStore();

  return (
    <Box sx={{ display: 'flex' }}>
      <Header />
      <Sidebar />
      <Box
        component="main"
        sx={{
          flexGrow: 1,
          p: 3,
          width: { sm: `calc(100% - ${sidebarOpen ? DRAWER_WIDTH : 0}px)` },
          ml: { sm: sidebarOpen ? 0 : `-${DRAWER_WIDTH}px` },
          transition: (theme) =>
            theme.transitions.create(['margin', 'width'], {
              easing: theme.transitions.easing.sharp,
              duration: theme.transitions.duration.leavingScreen,
            }),
        }}
      >
        {/* Header 아래 공간 확보를 위한 Toolbar */}
        <Toolbar />
        {/* 자식 라우트 콘텐츠가 렌더링되는 영역 */}
        <Outlet />
      </Box>
    </Box>
  );
};

export default MainLayout;
