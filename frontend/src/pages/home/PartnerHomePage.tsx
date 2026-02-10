/**
 * @fileoverview 펫시터 홈 페이지
 */

import React, { useEffect } from 'react';
import { Container, Box, Typography, Button } from '@mui/material';
import {
  TodayScheduleCard,
  NewRequestBadge,
  RevenueSummaryCard,
  NoticeList,
} from '../../components/home';
import { usePartnerHome } from '../../hooks/usePartnerHome';

const PartnerHomePage: React.FC = () => {
  const { data, loading, error, refetch } = usePartnerHome();

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
        <TodayScheduleCard schedule={data?.todaySchedule ?? null} />
        <NewRequestBadge count={data?.newRequestCount ?? 0} />
        <RevenueSummaryCard summary={data?.revenueSummary ?? null} />
        <NoticeList notices={data?.notices ?? []} />
      </Box>
    </Container>
  );
};

export default PartnerHomePage;
