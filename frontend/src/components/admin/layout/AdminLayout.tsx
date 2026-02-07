/**
 * @fileoverview 관리자 메인 레이아웃 컴포넌트
 *
 * 관리자 사이트의 레이아웃 구조를 정의합니다.
 * AdminHeader, AdminSidebar, 그리고 메인 콘텐츠 영역으로 구성됩니다.
 */

import React from 'react';
import { Outlet } from 'react-router-dom';
import { Box, Toolbar } from '@mui/material';
import AdminHeader from './AdminHeader';
import AdminSidebar from './AdminSidebar';
import { useUIStore } from '../../../stores/uiStore';

const DRAWER_WIDTH = 240;

const AdminLayout: React.FC = () => {
  const { sidebarOpen } = useUIStore();

  return (
    <Box sx={{ display: 'flex' }}>
      <AdminHeader />
      <AdminSidebar />
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
        <Toolbar />
        <Outlet />
      </Box>
    </Box>
  );
};

export default AdminLayout;
