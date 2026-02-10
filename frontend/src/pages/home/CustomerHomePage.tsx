/**
 * @fileoverview 반려인 홈 페이지
 */

import React, { useEffect } from 'react';
import { Container, Box, Typography, Button } from '@mui/material';
import {
  ActiveBookingBanner,
  RecommendedSitters,
  RecentSitters,
  EventBanners,
  CommunityFeeds,
} from '../../components/home';
import { useCustomerHome } from '../../hooks/useCustomerHome';

const CustomerHomePage: React.FC = () => {
  const { data, loading, error, refetch } = useCustomerHome();

  useEffect(() => {
    refetch();
  }, [refetch]);

  if (error) {
    return (
      <Container maxWidth="sm" sx={{ textAlign: 'center', py: 8 }}>
        <Typography color="error" gutterBottom>
          홈 화면을 불러올 수 없습니다
        </Typography>
        <Button variant="outlined" onClick={refetch}>
          재시도
        </Button>
      </Container>
    );
  }

  return (
    <Container maxWidth="sm" sx={{ pb: 10 }}>
      <Box sx={{ py: 2 }}>
        <ActiveBookingBanner booking={data?.activeBooking ?? null} />
        <RecommendedSitters sitters={data?.recommendedSitters ?? []} />
        <RecentSitters sitters={data?.recentSitters ?? []} />
        <EventBanners banners={data?.eventBanners ?? []} />
        <CommunityFeeds feeds={data?.communityFeeds ?? []} />
      </Box>
    </Container>
  );
};

export default CustomerHomePage;
