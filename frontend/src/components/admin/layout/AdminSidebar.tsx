/**
 * @fileoverview 관리자 사이드바 컴포넌트
 *
 * PetPro 관리자 전용 네비게이션 드로어입니다.
 * 8개 메인 메뉴와 하위 메뉴를 Collapse로 구현합니다.
 */

import React, { useState } from 'react';
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
  Collapse,
} from '@mui/material';
import {
  Dashboard,
  People,
  CalendarMonth,
  AccountBalance,
  Support,
  Article,
  BarChart,
  Settings,
  ExpandLess,
  ExpandMore,
} from '@mui/icons-material';
import { useUIStore } from '../../../stores/uiStore';

const DRAWER_WIDTH = 240;

interface SubMenuItem {
  text: string;
  path: string;
}

interface MenuItem {
  text: string;
  icon: React.ReactNode;
  path?: string;
  children?: SubMenuItem[];
}

const adminMenuItems: MenuItem[] = [
  {
    text: '대시보드',
    icon: <Dashboard />,
    path: '/admin/dashboard',
  },
  {
    text: '회원 관리',
    icon: <People />,
    children: [
      { text: '반려인 관리', path: '/admin/members/users' },
      { text: '펫시터 관리', path: '/admin/members/partners' },
      { text: '시터 심사', path: '/admin/members/partner-review' },
    ],
  },
  {
    text: '예약 관리',
    icon: <CalendarMonth />,
    children: [
      { text: '예약 현황', path: '/admin/reservations' },
      { text: '분쟁 관리', path: '/admin/reservations/disputes' },
    ],
  },
  {
    text: '정산 관리',
    icon: <AccountBalance />,
    children: [
      { text: '정산 리스트', path: '/admin/settlement' },
      { text: '수수료 설정', path: '/admin/settlement/fees' },
    ],
  },
  {
    text: '고객센터',
    icon: <Support />,
    children: [
      { text: '1:1 문의', path: '/admin/cs/inquiries' },
      { text: 'FAQ 관리', path: '/admin/cs/faq' },
    ],
  },
  {
    text: '콘텐츠 관리',
    icon: <Article />,
    children: [
      { text: '공지사항', path: '/admin/contents/notices' },
      { text: '이벤트', path: '/admin/contents/events' },
      { text: '커뮤니티', path: '/admin/contents/community' },
      { text: '캠페인', path: '/admin/contents/campaigns' },
    ],
  },
  {
    text: '통계',
    icon: <BarChart />,
    path: '/admin/statistics',
  },
  {
    text: '설정',
    icon: <Settings />,
    children: [
      { text: '알림 설정', path: '/admin/settings' },
      { text: '약관 관리', path: '/admin/settings/policies' },
      { text: '계정 관리', path: '/admin/settings/profile' },
      { text: '감사 로그', path: '/admin/settings/audit-log' },
      { text: '앱 버전', path: '/admin/settings/app-version' },
    ],
  },
];

const AdminSidebar: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { sidebarOpen } = useUIStore();

  const [openMenus, setOpenMenus] = useState<Record<string, boolean>>({});

  const handleToggle = (menuText: string) => {
    setOpenMenus((prev) => ({ ...prev, [menuText]: !prev[menuText] }));
  };

  const isActive = (path: string) => location.pathname === path;

  const isParentActive = (children?: SubMenuItem[]) =>
    children?.some((child) => location.pathname === child.path) ?? false;

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
          {adminMenuItems.map((item) => {
            if (item.children) {
              const parentActive = isParentActive(item.children);
              const isOpen = openMenus[item.text] ?? parentActive;

              return (
                <React.Fragment key={item.text}>
                  <ListItem disablePadding>
                    <ListItemButton
                      onClick={() => handleToggle(item.text)}
                      selected={parentActive}
                    >
                      <ListItemIcon>{item.icon}</ListItemIcon>
                      <ListItemText primary={item.text} />
                      {isOpen ? <ExpandLess /> : <ExpandMore />}
                    </ListItemButton>
                  </ListItem>
                  <Collapse in={isOpen} timeout="auto" unmountOnExit>
                    <List component="div" disablePadding>
                      {item.children.map((child) => (
                        <ListItemButton
                          key={child.path}
                          sx={{ pl: 4 }}
                          selected={isActive(child.path)}
                          onClick={() => navigate(child.path)}
                        >
                          <ListItemText primary={child.text} />
                        </ListItemButton>
                      ))}
                    </List>
                  </Collapse>
                </React.Fragment>
              );
            }

            return (
              <ListItem key={item.text} disablePadding>
                <ListItemButton
                  selected={isActive(item.path!)}
                  onClick={() => navigate(item.path!)}
                >
                  <ListItemIcon>{item.icon}</ListItemIcon>
                  <ListItemText primary={item.text} />
                </ListItemButton>
              </ListItem>
            );
          })}
        </List>
      </Box>
    </Drawer>
  );
};

export default AdminSidebar;
