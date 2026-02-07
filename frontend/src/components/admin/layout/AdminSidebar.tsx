/**
 * @fileoverview 관리자 사이드바 컴포넌트
 *
 * 관리자 전용 네비게이션 드로어입니다.
 * 관리자 메뉴만 표시합니다.
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
  Dashboard,
  People,
  Business,
  LocalShipping,
  Inventory,
} from '@mui/icons-material';
import { useUIStore } from '../../../stores/uiStore';

const DRAWER_WIDTH = 240;

interface MenuItem {
  text: string;
  icon: React.ReactNode;
  path: string;
}

const adminMenuItems: MenuItem[] = [
  { text: '대시보드', icon: <Dashboard />, path: '/admin/dashboard' },
  { text: '사용자 관리', icon: <People />, path: '/admin/users' },
  { text: '업체 관리', icon: <Business />, path: '/admin/companies' },
  { text: '공급사 관리', icon: <LocalShipping />, path: '/admin/suppliers' },
  { text: '재고 관리', icon: <Inventory />, path: '/admin/inventory' },
];

const AdminSidebar: React.FC = () => {
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
          {adminMenuItems.map((item) => (
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

export default AdminSidebar;
