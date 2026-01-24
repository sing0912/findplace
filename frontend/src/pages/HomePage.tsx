/**
 * @fileoverview 홈 페이지 컴포넌트
 *
 * 로그인한 사용자에게 표시되는 메인 대시보드 페이지입니다.
 * 주요 기능으로의 바로가기 카드들을 그리드 형태로 제공합니다.
 */

import React from 'react';
import {
  Container,
  Typography,
  Grid,
  Card,
  CardContent,
  CardActions,
  Button,
  Box,
} from '@mui/material';
import {
  Business,
  ShoppingCart,
  CalendarMonth,
  Pets,
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '../stores/authStore';

/**
 * 기능 카드 정보 배열
 * 홈 페이지에 표시될 주요 기능 카드들의 정보를 정의합니다.
 */
const features = [
  {
    /** 기능 아이콘 */
    icon: <Business sx={{ fontSize: 48, color: 'primary.main' }} />,
    /** 기능 제목 */
    title: '장례업체 찾기',
    /** 기능 설명 */
    description: '내 주변 반려동물 장례 업체를 찾아보세요.',
    /** 이동할 경로 */
    path: '/companies',
  },
  {
    icon: <ShoppingCart sx={{ fontSize: 48, color: 'primary.main' }} />,
    title: '장례용품',
    description: '다양한 장례용품을 구매하세요.',
    path: '/products',
  },
  {
    icon: <CalendarMonth sx={{ fontSize: 48, color: 'primary.main' }} />,
    title: '예약하기',
    description: '장례 서비스를 예약하세요.',
    path: '/reservations',
  },
  {
    icon: <Pets sx={{ fontSize: 48, color: 'primary.main' }} />,
    title: '추모관',
    description: '소중한 반려동물을 추모하세요.',
    path: '/memorial',
  },
];

/**
 * 홈 페이지 컴포넌트
 *
 * 로그인한 사용자를 환영하고 주요 기능으로의 바로가기를 제공합니다.
 *
 * 기능:
 * - 사용자 이름을 포함한 환영 메시지
 * - 4개의 주요 기능 카드 (장례업체, 장례용품, 예약, 추모관)
 * - 각 카드 클릭 시 해당 페이지로 이동
 */
const HomePage: React.FC = () => {
  const navigate = useNavigate();
  const { user } = useAuthStore();

  return (
    <Container maxWidth="lg">
      {/* 환영 메시지 섹션 */}
      <Box sx={{ my: 4 }}>
        <Typography variant="h4" component="h1" gutterBottom>
          안녕하세요{user ? `, ${user.name}님` : ''}
        </Typography>
        <Typography variant="subtitle1" color="textSecondary" gutterBottom>
          FindPlace에서 반려동물 장례 서비스를 이용해보세요.
        </Typography>
      </Box>

      {/* 기능 카드 그리드 */}
      <Grid container spacing={3}>
        {features.map((feature) => (
          <Grid size={{ xs: 12, sm: 6, md: 3 }} key={feature.title}>
            <Card
              sx={{
                height: '100%',
                display: 'flex',
                flexDirection: 'column',
                transition: 'transform 0.2s',
                '&:hover': {
                  transform: 'translateY(-4px)',
                },
              }}
            >
              <CardContent sx={{ flexGrow: 1, textAlign: 'center' }}>
                {/* 기능 아이콘 */}
                <Box sx={{ mb: 2 }}>{feature.icon}</Box>
                {/* 기능 제목 */}
                <Typography gutterBottom variant="h6" component="h2">
                  {feature.title}
                </Typography>
                {/* 기능 설명 */}
                <Typography variant="body2" color="textSecondary">
                  {feature.description}
                </Typography>
              </CardContent>
              {/* 바로가기 버튼 */}
              <CardActions>
                <Button
                  size="small"
                  fullWidth
                  onClick={() => navigate(feature.path)}
                >
                  바로가기
                </Button>
              </CardActions>
            </Card>
          </Grid>
        ))}
      </Grid>
    </Container>
  );
};

export default HomePage;
