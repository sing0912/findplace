/**
 * @fileoverview 사이드바 컴포넌트
 *
 * 좌측 네비게이션 드로어 컴포넌트입니다.
 * 일반 메뉴와 관리자 메뉴를 사용자 권한에 따라 표시합니다.
 */

import React from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import {
  Drawer,
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Toolbar,
  Divider,
  Box,
} from '@mui/material';
import {
  Home,
  Business,
  ShoppingCart,
  LocalShipping,
  People,
  Inventory,
  Receipt,
  CalendarMonth,
  Dashboard,
  LocationOn,
} from '@mui/icons-material';
import { useUIStore } from '../../stores/uiStore';
import { useAuthStore } from '../../stores/authStore';

/** 사이드바 너비 (픽셀) */
const DRAWER_WIDTH = 240;

/**
 * 메뉴 항목 인터페이스
 * 사이드바에 표시되는 각 메뉴 항목의 구조를 정의합니다.
 */
interface MenuItem {
  /** 메뉴 표시 텍스트 */
  text: string;
  /** 메뉴 아이콘 */
  icon: React.ReactNode;
  /** 이동할 경로 */
  path: string;
  /** 접근 가능한 역할 목록 (미지정 시 모든 사용자 접근 가능) */
  roles?: string[];
}

/**
 * 일반 사용자 메뉴 항목 목록
 * 모든 인증된 사용자가 접근할 수 있는 메뉴입니다.
 */
const menuItems: MenuItem[] = [
  { text: '홈', icon: <Home />, path: '/' },
  { text: '내 주변 장례식장', icon: <LocationOn />, path: '/nearby' },
  { text: '장례업체', icon: <Business />, path: '/companies' },
  { text: '상품', icon: <ShoppingCart />, path: '/products' },
  { text: '예약', icon: <CalendarMonth />, path: '/reservations' },
  { text: '주문', icon: <Receipt />, path: '/orders' },
];

/**
 * 관리자 메뉴 항목 목록
 * 관리자 권한이 있는 사용자만 접근할 수 있는 메뉴입니다.
 */
const adminMenuItems: MenuItem[] = [
  { text: '대시보드', icon: <Dashboard />, path: '/admin/dashboard', roles: ['ADMIN', 'SUPER_ADMIN'] },
  { text: '사용자 관리', icon: <People />, path: '/admin/users', roles: ['ADMIN', 'SUPER_ADMIN'] },
  { text: '업체 관리', icon: <Business />, path: '/admin/companies', roles: ['ADMIN', 'SUPER_ADMIN'] },
  { text: '공급사 관리', icon: <LocalShipping />, path: '/admin/suppliers', roles: ['ADMIN', 'SUPER_ADMIN'] },
  { text: '재고 관리', icon: <Inventory />, path: '/admin/inventory', roles: ['ADMIN', 'SUPER_ADMIN', 'SUPPLIER_ADMIN'] },
];

/**
 * 사이드바 컴포넌트
 *
 * 좌측에 고정되는 네비게이션 드로어입니다.
 * - 일반 메뉴: 홈, 장례업체, 상품, 예약, 주문
 * - 관리자 메뉴: 대시보드, 사용자/업체/공급사/재고 관리
 *
 * 사용자의 역할에 따라 관리자 메뉴의 표시 여부가 결정됩니다.
 */
const Sidebar: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { sidebarOpen } = useUIStore();
  const { user } = useAuthStore();

  /**
   * 메뉴 접근 권한 확인 함수
   * @param roles - 접근 가능한 역할 목록
   * @returns 접근 가능 여부
   */
  const hasAccess = (roles?: string[]) => {
    if (!roles) return true;
    if (!user) return false;
    return roles.includes(user.role);
  };

  /** 사용자가 접근 가능한 관리자 메뉴만 필터링 */
  const filteredAdminMenuItems = adminMenuItems.filter((item) => hasAccess(item.roles));

  return (
    <Drawer
      variant="persistent"
      open={sidebarOpen}
      sx={{
        width: sidebarOpen ? DRAWER_WIDTH : 0,
        flexShrink: 0,
        '& .MuiDrawer-paper': {
          width: DRAWER_WIDTH,
          boxSizing: 'border-box',
        },
      }}
    >
      {/* Header 높이만큼 공간 확보 */}
      <Toolbar />
      <Box sx={{ overflow: 'auto' }}>
        {/* 일반 메뉴 목록 */}
        <List>
          {menuItems.map((item) => (
            <ListItem key={item.text} disablePadding>
              <ListItemButton
                selected={location.pathname === item.path}
                onClick={() => navigate(item.path)}
              >
                <ListItemIcon>{item.icon}</ListItemIcon>
                <ListItemText primary={item.text} />
              </ListItemButton>
            </ListItem>
          ))}
        </List>

        {/* 관리자 메뉴 목록 (권한이 있는 경우에만 표시) */}
        {filteredAdminMenuItems.length > 0 && (
          <>
            <Divider />
            <List>
              {filteredAdminMenuItems.map((item) => (
                <ListItem key={item.text} disablePadding>
                  <ListItemButton
                    selected={location.pathname === item.path}
                    onClick={() => navigate(item.path)}
                  >
                    <ListItemIcon>{item.icon}</ListItemIcon>
                    <ListItemText primary={item.text} />
                  </ListItemButton>
                </ListItem>
              ))}
            </List>
          </>
        )}
      </Box>
    </Drawer>
  );
};

export default Sidebar;
