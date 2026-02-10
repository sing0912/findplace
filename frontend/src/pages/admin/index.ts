// Admin root pages
export { default as AdminLoginPage } from './AdminLoginPage';
export { default as DashboardPage } from './DashboardPage';

// 회원 관리
export { default as UserManagementPage } from './members/UserManagementPage';
export { default as PartnerManagementPage } from './members/PartnerManagementPage';
export { default as PartnerReviewPage } from './members/PartnerReviewPage';

// 예약 관리
export { default as ReservationManagementPage } from './reservations/ReservationManagementPage';
export { default as DisputeManagementPage } from './reservations/DisputeManagementPage';

// 정산 관리
export { default as SettlementPage } from './settlement/SettlementPage';
export { default as FeeSettingPage } from './settlement/FeeSettingPage';

// 고객센터
export { default as InquiryManagementPage } from './cs/InquiryManagementPage';
export { default as FaqManagementPage } from './cs/FaqManagementPage';

// 콘텐츠 관리
export { default as NoticeManagementPage } from './contents/NoticeManagementPage';
export { default as EventManagementPage } from './contents/EventManagementPage';
export { default as CommunityManagementPage } from './contents/CommunityManagementPage';
export { default as CampaignManagementPage } from './contents/CampaignManagementPage';

// 통계
export { default as StatisticsPage } from './statistics/StatisticsPage';

// 설정
export { default as SettingsPage } from './settings/SettingsPage';
export { default as AdminPolicyManagementPage } from './settings/PolicyManagementPage';
export { default as AdminProfilePage } from './settings/AdminProfilePage';
export { default as AuditLogPage } from './settings/AuditLogPage';
export { default as AppVersionPage } from './settings/AppVersionPage';
