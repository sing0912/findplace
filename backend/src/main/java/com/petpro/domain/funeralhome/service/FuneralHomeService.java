package com.petpro.domain.funeralhome.service;

import com.petpro.domain.funeralhome.dto.FuneralHomeRequest;
import com.petpro.domain.funeralhome.dto.FuneralHomeResponse;
import com.petpro.domain.funeralhome.entity.FuneralHome;
import com.petpro.domain.funeralhome.entity.FuneralHomeSyncLog;
import com.petpro.domain.funeralhome.repository.FuneralHomeRepository;
import com.petpro.domain.funeralhome.repository.FuneralHomeSyncLogRepository;
import com.petpro.global.exception.BusinessException;
import com.petpro.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 장례식장 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FuneralHomeService {

    private final FuneralHomeRepository funeralHomeRepository;
    private final FuneralHomeSyncLogRepository syncLogRepository;

    /**
     * 근처 장례식장 검색
     */
    @Cacheable(value = "funeralHomes",
            key = "'nearby:' + #request.latitude + ':' + #request.longitude + ':' + #request.radius")
    public FuneralHomeResponse.NearbyResult findNearby(FuneralHomeRequest.NearbySearch request) {
        List<Object[]> results = funeralHomeRepository.findNearbyWithDistance(
                request.getLatitude(),
                request.getLongitude(),
                request.getRadius(),
                request.getLimit(),
                request.getHasCrematorium(),
                request.getHasFuneral(),
                request.getHasColumbarium()
        );

        List<FuneralHomeResponse.ListItem> items = new ArrayList<>();
        for (Object[] row : results) {
            FuneralHome home = mapToFuneralHome(row);
            Double distance = ((Number) row[13]).doubleValue();  // distance는 인덱스 13
            items.add(FuneralHomeResponse.ListItem.from(home, Math.round(distance * 10.0) / 10.0));
        }

        return FuneralHomeResponse.NearbyResult.builder()
                .content(items)
                .totalCount(items.size())
                .radius(request.getRadius())
                .build();
    }

    /**
     * 네이티브 쿼리 결과를 FuneralHome 엔티티로 매핑
     *
     * 컬럼 순서 (FuneralHomeRepository.findNearbyWithDistance 참고):
     * 0: id, 1: name, 2: road_address, 3: lot_address, 4: phone,
     * 5: loc_code, 6: loc_name, 7: has_crematorium, 8: has_columbarium,
     * 9: has_funeral, 10: latitude, 11: longitude, 12: is_active, 13: distance
     */
    private FuneralHome mapToFuneralHome(Object[] row) {
        return FuneralHome.builder()
                .id(((Number) row[0]).longValue())
                .name((String) row[1])
                .roadAddress((String) row[2])
                .lotAddress((String) row[3])
                .phone((String) row[4])
                .locCode((String) row[5])
                .locName((String) row[6])
                .hasCrematorium((Boolean) row[7])
                .hasColumbarium((Boolean) row[8])
                .hasFuneral((Boolean) row[9])
                .latitude(row[10] != null ? (BigDecimal) row[10] : null)
                .longitude(row[11] != null ? (BigDecimal) row[11] : null)
                .isActive((Boolean) row[12])
                .build();
    }

    /**
     * 장례식장 상세 조회
     */
    @Cacheable(value = "funeralHomes", key = "'detail:' + #id")
    public FuneralHomeResponse.Detail findById(Long id) {
        FuneralHome home = funeralHomeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.FUNERAL_HOME_NOT_FOUND));

        return FuneralHomeResponse.Detail.from(home);
    }

    /**
     * 장례식장 목록 조회 (검색)
     */
    public Page<FuneralHomeResponse.ListItem> findBySearchConditions(
            FuneralHomeRequest.ListSearch request) {
        PageRequest pageable = PageRequest.of(request.getPage(), request.getSize());

        Page<FuneralHome> homes = funeralHomeRepository.findBySearchConditions(
                request.getKeyword(),
                request.getLocCode(),
                request.getHasCrematorium(),
                request.getHasFuneral(),
                request.getHasColumbarium(),
                request.getIsActive(),
                pageable
        );

        return homes.map(FuneralHomeResponse.ListItem::from);
    }

    /**
     * 장례식장 상태 변경 (관리자)
     */
    @Transactional
    @CacheEvict(value = "funeralHomes", allEntries = true)
    public void updateStatus(Long id, boolean isActive) {
        FuneralHome home = funeralHomeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.FUNERAL_HOME_NOT_FOUND));

        if (isActive) {
            home.activate();
        } else {
            home.deactivate();
        }

        funeralHomeRepository.save(home);
        log.info("Funeral home status updated: id={}, isActive={}", id, isActive);
    }

    /**
     * 동기화 로그 조회
     */
    public Page<FuneralHomeResponse.SyncLogItem> getSyncLogs(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        Page<FuneralHomeSyncLog> logs = syncLogRepository.findAllByOrderByStartedAtDesc(pageable);
        return logs.map(FuneralHomeResponse.SyncLogItem::from);
    }

    /**
     * 캐시 삭제
     */
    @CacheEvict(value = "funeralHomes", allEntries = true)
    public void evictCache() {
        log.info("Funeral home cache evicted");
    }

    /**
     * 통계 조회
     */
    public FuneralHomeStats getStats() {
        long totalActive = funeralHomeRepository.countByIsActiveTrue();
        long withCoordinates = funeralHomeRepository.countWithCoordinates();

        return new FuneralHomeStats(totalActive, withCoordinates);
    }

    public record FuneralHomeStats(long totalActive, long withCoordinates) {
        public double getCoordinateRate() {
            return totalActive > 0 ? (double) withCoordinates / totalActive * 100 : 0;
        }
    }
}
