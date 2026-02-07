package com.petpro.domain.region.service;

import com.petpro.domain.region.dto.RegionResponse.*;
import com.petpro.domain.region.entity.RegionCode;
import com.petpro.domain.region.entity.RegionType;
import com.petpro.domain.region.repository.RegionCodeRepository;
import com.petpro.global.exception.EntityNotFoundException;
import com.petpro.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegionCodeService {

    private final RegionCodeRepository regionCodeRepository;

    /**
     * 광역시/도 목록 조회
     */
    @Cacheable(value = "regions", key = "'metros'")
    public MetroListDto getMetros() {
        List<RegionCode> metros = regionCodeRepository
                .findByTypeAndIsActiveTrueOrderBySortOrder(RegionType.METRO);

        List<MetroDto> metroDtos = metros.stream()
                .map(metro -> {
                    long cityCount = regionCodeRepository.countCitiesByMetroCode(metro.getCode());
                    return MetroDto.of(metro, cityCount);
                })
                .toList();

        return MetroListDto.builder()
                .metros(metroDtos)
                .totalCount(metroDtos.size())
                .build();
    }

    /**
     * 특정 광역시/도의 시/군/구 목록 조회
     */
    @Cacheable(value = "regions", key = "'cities:' + #metroCode")
    public CityListDto getCities(String metroCode) {
        RegionCode metro = regionCodeRepository.findByCode(metroCode)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.REGION_NOT_FOUND));

        List<RegionCode> cities = regionCodeRepository
                .findByParentCodeAndIsActiveTrueOrderBySortOrder(metroCode);

        List<CityDto> cityDtos = cities.stream()
                .map(CityDto::from)
                .toList();

        return CityListDto.builder()
                .metroCode(metro.getCode())
                .metroName(metro.getName())
                .cities(cityDtos)
                .totalCount(cityDtos.size())
                .build();
    }

    /**
     * 지역 코드로 조회
     */
    public RegionDto getByCode(String code) {
        RegionCode region = regionCodeRepository.findByCode(code)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.REGION_NOT_FOUND));
        return RegionDto.from(region);
    }

    /**
     * 계층 구조로 전체 지역 조회
     */
    @Cacheable(value = "regions", key = "'hierarchy'")
    public HierarchyListDto getHierarchy() {
        List<RegionCode> metros = regionCodeRepository
                .findByTypeAndIsActiveTrueOrderBySortOrder(RegionType.METRO);

        List<HierarchyDto> hierarchyDtos = metros.stream()
                .map(metro -> {
                    List<RegionCode> cities = regionCodeRepository
                            .findByParentCodeAndIsActiveTrueOrderBySortOrder(metro.getCode());
                    return HierarchyDto.of(metro, cities);
                })
                .toList();

        return HierarchyListDto.builder()
                .regions(hierarchyDtos)
                .build();
    }

    /**
     * 시/군/구 코드로 광역시/도 코드 조회
     */
    public String getMetroCode(String cityCode) {
        return regionCodeRepository.findByCode(cityCode)
                .map(RegionCode::getParentCode)
                .orElse(null);
    }

    /**
     * 광역시/도 코드로 모든 시/군/구 코드 목록 조회
     */
    public List<String> getAllCityCodes(String metroCode) {
        return regionCodeRepository.findByParentCode(metroCode)
                .stream()
                .map(RegionCode::getCode)
                .toList();
    }

    /**
     * 전체 활성 지역 목록 조회
     */
    public List<RegionDto> getAllActive() {
        return regionCodeRepository.findAllActive()
                .stream()
                .map(RegionDto::from)
                .toList();
    }

    /**
     * 지역 코드 존재 여부 확인
     */
    public boolean existsByCode(String code) {
        return regionCodeRepository.existsByCode(code);
    }

    /**
     * 캐시 삭제
     */
    @CacheEvict(value = "regions", allEntries = true)
    public void evictCache() {
        // 지역 코드 변경 시 캐시 삭제
    }
}
