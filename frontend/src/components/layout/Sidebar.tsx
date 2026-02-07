/**
 * @fileoverview 사이드바 컴포넌트
 *
 * 좌측 네비게이션 드로어 컴포넌트입니다.
 * 일반 사용자 메뉴만 표시합니다.
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
  Box,
} from '@mui/material';
import {
  Home,
  Search,
  CalendarMonth,
  ChatBubble,
  Person,
} from '@mui/icons-material';
import { useUIStore } from '../../stores/uiStore';

/** 사이드바 너비 (픽셀) */
const DRAWER_WIDTH = 240;

interface MenuItem {
  text: string;
  icon: React.ReactNode;
  path: string;
}

/**
 * PetPro IA 기준 사용자 메뉴 항목 목록
 * @see docs/develop/user/frontend.md - 섹션 11.1
 */
const menuItems: MenuItem[] = [
  { text: '홈', icon: <Home />, path: '/' },
  { text: '시터 검색', icon: <Search />, path: '/search' },
  { text: '예약', icon: <CalendarMonth />, path: '/reservations' },
  { text: '채팅', icon: <ChatBubble />, path: '/chat' },
  { text: '마이', icon: <Person />, path: '/mypage' },
];

/**
 * 사이드바 컴포넌트
 *
 * 좌측에 고정되는 네비게이션 드로어입니다.
 * 일반 사용자 메뉴만 표시합니다.
 */
const Sidebar: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { sidebarOpen } = useUIStore();

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
      <Toolbar />
      <Box sx={{ overflow: 'auto' }}>
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
      </Box>
    </Drawer>
  );
};

export default Sidebar;
