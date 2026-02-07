package com.petpro.domain.log.service;

import com.petpro.domain.log.entity.UserDemographicsSnapshot;
import com.petpro.domain.log.repository.UserDemographicsSnapshotRepository;
import com.petpro.domain.user.entity.User;
import com.petpro.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

/**
 * 인구통계 스냅샷 서비스
 *
 * 매일 02시에 메인DB 사용자 데이터를 조회하여 로그DB에 스냅샷을 저장합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DemographicsSnapshotService {

    private final UserRepository userRepository;
    private final UserDemographicsSnapshotRepository snapshotRepository;

    /**
     * 일일 인구통계 스냅샷 생성
     * 매일 02:00에 실행
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void createDailySnapshot() {
        LocalDate today = LocalDate.now();
        log.info("인구통계 스냅샷 생성 시작: date={}", today);

        try {
            // 활성 사용자를 페이지 단위로 조회하여 스냅샷 생성
            int page = 0;
            int pageSize = 500;
            int count = 0;
            Page<User> userPage;

            do {
                userPage = userRepository.findAllActive(PageRequest.of(page, pageSize));

                for (User user : userPage.getContent()) {
                    try {
                        String ageGroup = calculateAgeGroup(user.getBirthDate());

                        UserDemographicsSnapshot snapshot = UserDemographicsSnapshot.create(
                                user.getId(),
                                ageGroup,
                                null,
                                null,
                                today
                        );

                        saveSnapshot(snapshot);
                        count++;
                    } catch (Exception e) {
                        log.warn("사용자 스냅샷 생성 실패: userId={}, error={}", user.getId(), e.getMessage());
                    }
                }
                page++;
            } while (userPage.hasNext());

            log.info("인구통계 스냅샷 생성 완료: date={}, count={}", today, count);
        } catch (Exception e) {
            log.error("인구통계 스냅샷 생성 실패: date={}, error={}", today, e.getMessage(), e);
        }
    }

    @Transactional("logTransactionManager")
    protected void saveSnapshot(UserDemographicsSnapshot snapshot) {
        snapshotRepository.save(snapshot);
    }

    private String calculateAgeGroup(LocalDate birthDate) {
        if (birthDate == null) {
            return "UNKNOWN";
        }
        int age = Period.between(birthDate, LocalDate.now()).getYears();
        if (age < 20) return "10s";
        if (age < 30) return "20s";
        if (age < 40) return "30s";
        if (age < 50) return "40s";
        if (age < 60) return "50s";
        return "60s+";
    }
}
