package com.petpro.domain.home.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class HomeResponse {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerHome {
        private ActiveBookingDto activeBooking;
        private List<RecommendedSitterDto> recommendedSitters;
        private List<RecentSitterDto> recentSitters;
        private List<EventBannerDto> eventBanners;
        private List<CommunityFeedDto> communityFeeds;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PartnerHome {
        private TodayScheduleDto todaySchedule;
        private int newRequestCount;
        private RevenueSummaryDto revenueSummary;
        private List<NoticeItemDto> notices;
    }

    // === Customer Home DTOs ===

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActiveBookingDto {
        private Long bookingId;
        private String bookingNumber;
        private String partnerNickname;
        private String partnerProfileImageUrl;
        private String serviceType;
        private String serviceTypeName;
        private String startDate;
        private String endDate;
        private String status;
        private List<String> petNames;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendedSitterDto {
        private Long id;
        private String nickname;
        private String profileImageUrl;
        private String address;
        private Double distance;
        private double averageRating;
        private int reviewCount;
        private int completedBookingCount;
        private List<SitterServiceDto> services;
        private List<String> acceptablePetTypes;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SitterServiceDto {
        private String serviceType;
        private String serviceTypeName;
        private int basePrice;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentSitterDto {
        private Long id;
        private String nickname;
        private String profileImageUrl;
        private double averageRating;
        private String lastBookingDate;
        private String serviceType;
        private String serviceTypeName;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventBannerDto {
        private Long id;
        private String title;
        private String imageUrl;
        private String linkUrl;
        private String type;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommunityFeedDto {
        private Long id;
        private String title;
        private String category;
        private String categoryName;
        private int likeCount;
        private int commentCount;
        private String thumbnailUrl;
        private String createdAt;
    }

    // === Partner Home DTOs ===

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TodayScheduleDto {
        private String date;
        private int totalCount;
        private List<TodayBookingDto> bookings;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TodayBookingDto {
        private Long bookingId;
        private String bookingNumber;
        private String customerName;
        private String serviceType;
        private String serviceTypeName;
        private String startDate;
        private String endDate;
        private String status;
        private List<String> petNames;
        private int petCount;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueSummaryDto {
        private String month;
        private long totalAmount;
        private int completedBookingCount;
        private long pendingAmount;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NoticeItemDto {
        private Long id;
        private String title;
        private String createdAt;
    }
}
