/**
 * @fileoverview 펫시터 하단 탭 네비게이션
 */

import React from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { BottomNavigation, BottomNavigationAction, Paper } from '@mui/material';
import { Home, CalendarMonth, Assignment, ChatBubble, Person } from '@mui/icons-material';

const tabs = [
  { label: '홈', icon: <Home />, path: '/partner' },
  { label: '일정', icon: <CalendarMonth />, path: '/partner/calendar' },
  { label: '예약관리', icon: <Assignment />, path: '/partner/bookings' },
  { label: '채팅', icon: <ChatBubble />, path: '/partner/chat' },
  { label: '마이', icon: <Person />, path: '/partner/my' },
];

const PartnerTabBar: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const currentIndex = tabs.findIndex((tab) => tab.path === location.pathname);

  return (
    <Paper sx={{ position: 'fixed', bottom: 0, left: 0, right: 0, zIndex: 1000 }} elevation={3}>
      <BottomNavigation
        value={currentIndex >= 0 ? currentIndex : 0}
        onChange={(_, newValue) => navigate(tabs[newValue].path)}
        showLabels
      >
        {tabs.map((tab) => (
          <BottomNavigationAction key={tab.path} label={tab.label} icon={tab.icon} />
        ))}
      </BottomNavigation>
    </Paper>
  );
};

export default PartnerTabBar;
