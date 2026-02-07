package com.petpro.domain.funeralhome.scheduler;

import com.petpro.domain.funeralhome.service.FuneralHomeSyncService;
import com.petpro.domain.funeralhome.service.GovApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 장례식장 동기화 스케줄러
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FuneralHomeSyncScheduler {

    private final FuneralHomeSyncService syncService;
    private final GovApiService govApiService;

    /**
     * 증분 동기화 - 매일 02:00 실행
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void runIncrementalSync() {
        log.info("Starting scheduled incremental sync");
        try {
            var result = syncService.runIncrementalSync();
            log.info("Scheduled incremental sync completed: status={}, inserted={}, updated={}",
                    result.getStatus(), result.getInsertedCount(), result.getUpdatedCount());
        } catch (Exception e) {
            log.error("Scheduled incremental sync failed", e);
        }
    }

    /**
     * 전체 동기화 - 매주 일요일 03:00 실행
     */
    @Scheduled(cron = "0 0 3 * * SUN")
    public void runFullSync() {
        log.info("Starting scheduled full sync");
        try {
            var result = syncService.runFullSync();
            log.info("Scheduled full sync completed: status={}, inserted={}, updated={}, deleted={}",
                    result.getStatus(), result.getInsertedCount(),
                    result.getUpdatedCount(), result.getDeletedCount());
        } catch (Exception e) {
            log.error("Scheduled full sync failed", e);
        }
    }

    /**
     * API 호출 카운트 리셋 - 매일 자정 실행
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void resetApiCallCount() {
        log.info("Resetting daily API call count");
        govApiService.resetDailyCount();
    }
}
