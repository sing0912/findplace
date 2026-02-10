/**
 * @fileoverview 홈 페이지 - 역할 기반 라우팅
 *
 * CUSTOMER → CustomerHomePage, PARTNER → PartnerHomePage
 */

import React from 'react';
import { useAuthStore } from '../stores/authStore';
import { CustomerHomePage } from './home';
import { PartnerHomePage } from './home';

const HomePage: React.FC = () => {
  const { user } = useAuthStore();

  if (user?.role === 'PARTNER') {
    return <PartnerHomePage />;
  }

  return <CustomerHomePage />;
};

export default HomePage;
