/**
 * @fileoverview Home 도메인 타입 정의
 */

// === Customer Home ===

export interface CustomerHomeResponse {
  activeBooking: ActiveBooking | null;
  recommendedSitters: RecommendedSitter[];
  recentSitters: RecentSitter[];
  eventBanners: EventBanner[];
  communityFeeds: CommunityFeed[];
}

export interface ActiveBooking {
  bookingId: number;
  bookingNumber: string;
  partnerNickname: string;
  partnerProfileImageUrl: string | null;
  serviceType: string;
  serviceTypeName: string;
  startDate: string;
  endDate: string;
  status: string;
  petNames: string[];
}

export interface RecommendedSitter {
  id: number;
  nickname: string;
  profileImageUrl: string | null;
  address: string;
  distance: number | null;
  averageRating: number;
  reviewCount: number;
  completedBookingCount: number;
  services: SitterServiceSummary[];
  acceptablePetTypes: string[];
}

export interface SitterServiceSummary {
  serviceType: string;
  serviceTypeName: string;
  basePrice: number;
}

export interface RecentSitter {
  id: number;
  nickname: string;
  profileImageUrl: string | null;
  averageRating: number;
  lastBookingDate: string;
  serviceType: string;
  serviceTypeName: string;
}

export interface EventBanner {
  id: number;
  title: string;
  imageUrl: string | null;
  linkUrl: string;
  type: 'EVENT' | 'NOTICE';
}

export interface CommunityFeed {
  id: number;
  title: string;
  category: string;
  categoryName: string;
  likeCount: number;
  commentCount: number;
  thumbnailUrl: string | null;
  createdAt: string;
}

// === Partner Home ===

export interface PartnerHomeResponse {
  todaySchedule: TodaySchedule;
  newRequestCount: number;
  revenueSummary: RevenueSummary;
  notices: NoticeItem[];
}

export interface TodaySchedule {
  date: string;
  totalCount: number;
  bookings: TodayBooking[];
}

export interface TodayBooking {
  bookingId: number;
  bookingNumber: string;
  customerName: string;
  serviceType: string;
  serviceTypeName: string;
  startDate: string;
  endDate: string;
  status: string;
  petNames: string[];
  petCount: number;
}

export interface RevenueSummary {
  month: string;
  totalAmount: number;
  completedBookingCount: number;
  pendingAmount: number;
}

export interface NoticeItem {
  id: number;
  title: string;
  createdAt: string;
}
