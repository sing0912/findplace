package com.findplace.domain.funeralhome.service;

import com.findplace.domain.funeralhome.dto.FuneralHomeResponse;
import com.findplace.domain.funeralhome.dto.GovApiResponse;
import com.findplace.domain.funeralhome.entity.FuneralHome;
import com.findplace.domain.funeralhome.entity.FuneralHomeSyncLog;
import com.findplace.domain.funeralhome.entity.SyncStatus;
import com.findplace.domain.funeralhome.entity.SyncType;
import com.findplace.domain.funeralhome.repository.FuneralHomeRepository;
import com.findplace.domain.funeralhome.repository.FuneralHomeSyncLogRepository;
import com.findplace.domain.location.dto.GeocodingResult;
import com.findplace.domain.location.service.GeocodingService;
import com.findplace.global.exception.BusinessException;
import com.findplace.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 장례식장 동기화 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FuneralHomeSyncService {

    private final GovApiService govApiService;
    private final GeocodingService geocodingService;
    private final FuneralHomeRepository funeralHomeRepository;
    private final FuneralHomeSyncLogRepository syncLogRepository;

    private static final int PAGE_SIZE = 100;

    /**
     * 증분 동기화 실행
     */
    @Transactional
    public FuneralHomeResponse.SyncResult runIncrementalSync() {
        // 이미 실행 중인 동기화가 있는지 확인
        if (syncLogRepository.existsByStatus(SyncStatus.RUNNING)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        FuneralHomeSyncLog syncLog = FuneralHomeSyncLog.start(SyncType.INCREMENTAL);
        syncLog = syncLogRepository.save(syncLog);

        try {
            SyncResult result = executeSync();

            syncLog.complete(result.totalCount, result.insertedCount,
                    result.updatedCount, result.deletedCount);
            syncLogRepository.save(syncLog);

            // 좌표 없는 항목 Geocoding 비동기 처리
            geocodeMissingCoordinates();

            return FuneralHomeResponse.SyncResult.builder()
                    .logId(syncLog.getId())
                    .syncType(SyncType.INCREMENTAL)
                    .status(SyncStatus.COMPLETED)
                    .totalCount(result.totalCount)
                    .insertedCount(result.insertedCount)
                    .updatedCount(result.updatedCount)
                    .deletedCount(result.deletedCount)
                    .message("증분 동기화 완료")
                    .build();

        } catch (Exception e) {
            log.error("Incremental sync failed", e);
            syncLog.fail(e.getMessage());
            syncLogRepository.save(syncLog);

            return FuneralHomeResponse.SyncResult.builder()
                    .logId(syncLog.getId())
                    .syncType(SyncType.INCREMENTAL)
                    .status(SyncStatus.FAILED)
                    .message("동기화 실패: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 전체 동기화 실행
     */
    @Transactional
    public FuneralHomeResponse.SyncResult runFullSync() {
        if (syncLogRepository.existsByStatus(SyncStatus.RUNNING)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        FuneralHomeSyncLog syncLog = FuneralHomeSyncLog.start(SyncType.FULL);
        syncLog = syncLogRepository.save(syncLog);

        try {
            // 기존 데이터의 syncedAt 기록
            LocalDateTime syncStartTime = LocalDateTime.now();

            SyncResult result = executeSync();

            // 동기화되지 않은 데이터 비활성화
            int deactivated = funeralHomeRepository.deactivateOldEntries(syncStartTime);
            result.deletedCount += deactivated;

            syncLog.complete(result.totalCount, result.insertedCount,
                    result.updatedCount, result.deletedCount);
            syncLogRepository.save(syncLog);

            // 좌표 없는 항목 Geocoding
            geocodeMissingCoordinates();

            return FuneralHomeResponse.SyncResult.builder()
                    .logId(syncLog.getId())
                    .syncType(SyncType.FULL)
                    .status(SyncStatus.COMPLETED)
                    .totalCount(result.totalCount)
                    .insertedCount(result.insertedCount)
                    .updatedCount(result.updatedCount)
                    .deletedCount(result.deletedCount)
                    .message("전체 동기화 완료")
                    .build();

        } catch (Exception e) {
            log.error("Full sync failed", e);
            syncLog.fail(e.getMessage());
            syncLogRepository.save(syncLog);

            return FuneralHomeResponse.SyncResult.builder()
                    .logId(syncLog.getId())
                    .syncType(SyncType.FULL)
                    .status(SyncStatus.FAILED)
                    .message("동기화 실패: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 동기화 실행 (공통 로직)
     */
    private SyncResult executeSync() {
        SyncResult result = new SyncResult();
        int pageNo = 1;
        boolean hasMore = true;

        Set<String> processedKeys = new HashSet<>();

        while (hasMore && govApiService.canCall()) {
            GovApiResponse response = govApiService.fetchFuneralHomes(pageNo, PAGE_SIZE);
            List<GovApiResponse.Item> items = response.getItems();

            if (items.isEmpty()) {
                hasMore = false;
                continue;
            }

            for (GovApiResponse.Item item : items) {
                try {
                    processItem(item, result, processedKeys);
                } catch (Exception e) {
                    log.warn("Failed to process item: name={}, error={}",
                            item.getNm(), e.getMessage());
                }
            }

            result.totalCount += items.size();
            pageNo++;

            // 마지막 페이지 확인
            if (items.size() < PAGE_SIZE) {
                hasMore = false;
            }
        }

        log.info("Sync completed: total={}, inserted={}, updated={}, deleted={}",
                result.totalCount, result.insertedCount, result.updatedCount, result.deletedCount);

        return result;
    }

    /**
     * 개별 항목 처리
     */
    private void processItem(GovApiResponse.Item item, SyncResult result, Set<String> processedKeys) {
        String key = item.getNm() + "|" + item.getLocCode();

        // 중복 방지
        if (processedKeys.contains(key)) {
            return;
        }
        processedKeys.add(key);

        Optional<FuneralHome> existing = funeralHomeRepository.findByNameAndLocCode(
                item.getNm(), item.getLocCode());

        if (existing.isPresent()) {
            // 기존 데이터 업데이트
            FuneralHome home = existing.get();
            home.updateFromApi(
                    item.getRoadAddr(),
                    item.getLotAddr(),
                    item.getTelno(),
                    item.hasCrematorium(),
                    item.hasColumbarium(),
                    item.hasFuneral()
            );
            funeralHomeRepository.save(home);
            result.updatedCount++;
        } else {
            // 신규 데이터 추가
            FuneralHome home = FuneralHome.fromApiResponse(
                    item.getNm(),
                    item.getRoadAddr(),
                    item.getLotAddr(),
                    item.getTelno(),
                    item.getLocCode(),
                    item.getLocName(),
                    item.hasCrematorium(),
                    item.hasColumbarium(),
                    item.hasFuneral()
            );
            funeralHomeRepository.save(home);
            result.insertedCount++;
        }
    }

    /**
     * 좌표가 없는 장례식장 Geocoding 처리 (비동기)
     */
    @Async
    public void geocodeMissingCoordinates() {
        List<FuneralHome> homes = funeralHomeRepository.findByLatitudeIsNullAndIsActiveTrue();
        log.info("Starting geocoding for {} funeral homes", homes.size());

        int successCount = 0;
        int failCount = 0;

        for (FuneralHome home : homes) {
            try {
                String address = home.getRoadAddress() != null
                        ? home.getRoadAddress() : home.getLotAddress();

                if (address == null || address.isBlank()) {
                    continue;
                }

                GeocodingResult result = geocodingService.geocode(address);
                if (result != null) {
                    home.setCoordinates(
                            BigDecimal.valueOf(result.getLatitude()),
                            BigDecimal.valueOf(result.getLongitude())
                    );
                    funeralHomeRepository.save(home);
                    successCount++;
                }

                // Rate limiting (Google API 제한)
                Thread.sleep(100);

            } catch (Exception e) {
                log.warn("Geocoding failed for: {}, error: {}", home.getName(), e.getMessage());
                failCount++;
            }
        }

        log.info("Geocoding completed: success={}, fail={}", successCount, failCount);
    }

    /**
     * 동기화 결과 내부 클래스
     */
    private static class SyncResult {
        int totalCount = 0;
        int insertedCount = 0;
        int updatedCount = 0;
        int deletedCount = 0;
    }
}
