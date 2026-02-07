/**
 * @fileoverview 헤더 컴포넌트
 *
 * 애플리케이션 상단에 고정되는 앱바(AppBar) 컴포넌트입니다.
 * 사이드바 토글 버튼, 로고, 사용자 메뉴 등을 포함합니다.
 */

import React from 'react';
import {
  AppBar,
  Toolbar,
  Typography,
  IconButton,
  Box,
  Avatar,
  Menu,
  MenuItem,
  Divider,
} from '@mui/material';
import {
  Menu as MenuIcon,
  AccountCircle,
  Logout,
  Settings,
  Person,
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '../../stores/authStore';
import { useUIStore } from '../../stores/uiStore';
import { useAuth } from '../../hooks/useAuth';

/**
 * 헤더 컴포넌트
 *
 * 화면 상단에 고정되어 다음 기능을 제공합니다:
 * - 사이드바 열기/닫기 토글 버튼
 * - 애플리케이션 로고 및 타이틀
 * - 사용자 프로필 메뉴 (설정, 로그아웃)
 */
const Header: React.FC = () => {
  const { user } = useAuthStore();
  const { toggleSidebar } = useUIStore();
  const { logout } = useAuth();
  const navigate = useNavigate();

  /** 사용자 메뉴 앵커 엘리먼트 */
  const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null);

  /**
   * 사용자 메뉴 열기 핸들러
   * @param event - 클릭 이벤트
   */
  const handleMenu = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  /**
   * 사용자 메뉴 닫기 핸들러
   */
  const handleClose = () => {
    setAnchorEl(null);
  };

  /**
   * 로그아웃 핸들러
   * 메뉴를 닫고 로그아웃을 실행합니다.
   */
  const handleLogout = () => {
    handleClose();
    logout();
  };

  return (
    <AppBar position="fixed" sx={{ zIndex: (theme) => theme.zIndex.drawer + 1 }}>
      <Toolbar>
        {/* 사이드바 토글 버튼 */}
        <IconButton
          color="inherit"
          aria-label="toggle sidebar"
          edge="start"
          onClick={toggleSidebar}
          sx={{ mr: 2 }}
        >
          <MenuIcon />
        </IconButton>

        {/* 애플리케이션 타이틀 */}
        <Typography variant="h6" noWrap component="div" sx={{ flexGrow: 1 }}>
          PetPro
        </Typography>

        {/* 사용자 메뉴 (로그인된 경우에만 표시) */}
        {user && (
          <Box>
            <IconButton
              size="large"
              aria-label="account of current user"
              aria-controls="menu-appbar"
              aria-haspopup="true"
              onClick={handleMenu}
              color="inherit"
            >
              {user.profileImageUrl ? (
                <Avatar src={user.profileImageUrl} sx={{ width: 32, height: 32 }} />
              ) : (
                <AccountCircle />
              )}
            </IconButton>
            <Menu
              id="menu-appbar"
              anchorEl={anchorEl}
              anchorOrigin={{
                vertical: 'bottom',
                horizontal: 'right',
              }}
              keepMounted
              transformOrigin={{
                vertical: 'top',
                horizontal: 'right',
              }}
              open={Boolean(anchorEl)}
              onClose={handleClose}
            >
              {/* 사용자 정보 표시 */}
              <MenuItem disabled>
                <Typography variant="body2">{user.name}</Typography>
              </MenuItem>
              <MenuItem disabled>
                <Typography variant="caption" color="textSecondary">
                  {user.email}
                </Typography>
              </MenuItem>
              <Divider />
              {/* 마이페이지 메뉴 */}
              <MenuItem onClick={() => { handleClose(); navigate('/mypage'); }}>
                <Person fontSize="small" sx={{ mr: 1 }} />
                마이페이지
              </MenuItem>
              {/* 설정 메뉴 */}
              <MenuItem onClick={handleClose}>
                <Settings fontSize="small" sx={{ mr: 1 }} />
                설정
              </MenuItem>
              {/* 로그아웃 메뉴 */}
              <MenuItem onClick={handleLogout}>
                <Logout fontSize="small" sx={{ mr: 1 }} />
                로그아웃
              </MenuItem>
            </Menu>
          </Box>
        )}
      </Toolbar>
    </AppBar>
  );
};

export default Header;
