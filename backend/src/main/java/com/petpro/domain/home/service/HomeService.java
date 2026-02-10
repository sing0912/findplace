package com.petpro.domain.home.service;

import com.petpro.domain.home.dto.HomeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;

/**
 * HomeService
 *
 * 홈 화면 데이터를 집계하여 제공하는 서비스입니다.
 * 현재는 하위 도메인(booking, sitter, community 등)이 미구현 상태이므로
 * 빈 데이터(stub)를 반환합니다.
 *
 * 향후 각 도메인이 구현되면 실제 데이터를 조회하도록 연동합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeService {

    /**
     * 반려인(CUSTOMER) 홈 화면 데이터를 조회합니다.
     *
     * @param userId 사용자 ID
     * @param latitude 현재 위치 위도 (nullable)
     * @param longitude 현재 위치 경도 (nullable)
     * @return 반려인 홈 응답 데이터
     */
    public HomeResponse.CustomerHome getCustomerHome(Long userId, Double latitude, Double longitude) {
        // TODO: booking 도메인 구현 시 activeBooking 연동
        // TODO: sitter 도메인 구현 시 recommendedSitters, recentSitters 연동
        // TODO: community 도메인 구현 시 communityFeeds 연동
        // TODO: coupon/event 도메인 구현 시 eventBanners 연동

        return HomeResponse.CustomerHome.builder()
                .activeBooking(null)
                .recommendedSitters(Collections.emptyList())
                .recentSitters(Collections.emptyList())
                .eventBanners(Collections.emptyList())
                .communityFeeds(Collections.emptyList())
                .build();
    }

    /**
     * 펫시터(PARTNER) 홈 화면 데이터를 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 펫시터 홈 응답 데이터
     */
    public HomeResponse.PartnerHome getPartnerHome(Long userId) {
        // TODO: booking 도메인 구현 시 todaySchedule, newRequestCount 연동
        // TODO: payout 도메인 구현 시 revenueSummary 연동
        // TODO: notification/faq 도메인 구현 시 notices 연동

        return HomeResponse.PartnerHome.builder()
                .todaySchedule(HomeResponse.TodayScheduleDto.builder()
                        .date(LocalDate.now().toString())
                        .totalCount(0)
                        .bookings(Collections.emptyList())
                        .build())
                .newRequestCount(0)
                .revenueSummary(HomeResponse.RevenueSummaryDto.builder()
                        .month(YearMonth.now().toString())
                        .totalAmount(0)
                        .completedBookingCount(0)
                        .pendingAmount(0)
                        .build())
                .notices(Collections.emptyList())
                .build();
    }
}
