/**
 * @fileoverview 관리자 헤더 컴포넌트
 *
 * 관리자 사이트 상단에 고정되는 앱바 컴포넌트입니다.
 * 타이틀 "PetPro 관리자"를 표시하고 사용자 메뉴를 제공합니다.
 */

import React from 'react';
import { useNavigate } from 'react-router-dom';
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
  Person,
} from '@mui/icons-material';
import { useAuthStore } from '../../../stores/authStore';
import { useUIStore } from '../../../stores/uiStore';
import { useAdminAuth } from '../../../hooks/useAdminAuth';

const AdminHeader: React.FC = () => {
  const navigate = useNavigate();
  const { user } = useAuthStore();
  const { toggleSidebar } = useUIStore();
  const { logout } = useAdminAuth();

  const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null);

  const handleMenu = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleClose = () => {
    setAnchorEl(null);
  };

  const handleMyPage = () => {
    handleClose();
    navigate('/admin/settings/profile');
  };

  const handleLogout = () => {
    handleClose();
    logout();
  };

  return (
    <AppBar position="fixed" sx={{ zIndex: (theme) => theme.zIndex.drawer + 1 }}>
      <Toolbar>
        <IconButton
          color="inherit"
          aria-label="toggle sidebar"
          edge="start"
          onClick={toggleSidebar}
          sx={{ mr: 2 }}
        >
          <MenuIcon />
        </IconButton>

        <Typography variant="h6" noWrap component="div" sx={{ flexGrow: 1 }}>
          PetPro 관리자
        </Typography>

        {user && (
          <Box>
            <IconButton
              size="large"
              aria-label="account of current user"
              aria-controls="admin-menu-appbar"
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
              id="admin-menu-appbar"
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
              <MenuItem disabled>
                <Typography variant="body2">{user.name}</Typography>
              </MenuItem>
              <MenuItem disabled>
                <Typography variant="caption" color="textSecondary">
                  {user.email}
                </Typography>
              </MenuItem>
              <Divider />
              <MenuItem onClick={handleMyPage}>
                <Person fontSize="small" sx={{ mr: 1 }} />
                마이페이지
              </MenuItem>
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

export default AdminHeader;
