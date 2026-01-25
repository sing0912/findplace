package com.findplace.domain.location.service;

import com.findplace.domain.location.config.GoogleMapsConfig;
import com.findplace.domain.location.dto.Coordinates;
import com.findplace.domain.location.dto.GeocodingResult;
import com.findplace.domain.location.dto.GeocodingResult.AddressComponents;
import com.findplace.domain.location.exception.GeocodingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

/**
 * 지오코딩 서비스
 * Google Maps Geocoding API를 사용하여 주소와 좌표 간 변환을 수행합니다.
 */
@Slf4j
@Service
public class GeocodingService {

    private static final String GEOCODING_URL = "https://maps.googleapis.com/maps/api/geocode/json";

    private final GoogleMapsConfig config;
    private final RestTemplate restTemplate;

    public GeocodingService(GoogleMapsConfig config) {
        this.config = config;
        this.restTemplate = new RestTemplate();
    }

    /**
     * 주소를 좌표로 변환 (Geocoding)
     *
     * @param address 주소
     * @return 지오코딩 결과
     */
    @Cacheable(value = "geocoding", key = "#address")
    public GeocodingResult geocode(String address) {
        if (!config.isConfigured()) {
            log.warn("Google Maps API key is not configured. Using mock response.");
            return createMockResult(address);
        }

        String url = UriComponentsBuilder.fromHttpUrl(GEOCODING_URL)
                .queryParam("address", address)
                .queryParam("key", config.getApiKey())
                .queryParam("language", config.getLanguage())
                .queryParam("region", config.getRegion())
                .build()
                .toUriString();

        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            return parseGeocodingResponse(response);
        } catch (Exception e) {
            log.error("Geocoding failed for address: {}", address, e);
            throw new GeocodingException("주소 변환 실패: " + address);
        }
    }

    /**
     * 좌표를 주소로 변환 (Reverse Geocoding)
     *
     * @param latitude 위도
     * @param longitude 경도
     * @return 지오코딩 결과
     */
    @Cacheable(value = "geocoding", key = "#latitude + ',' + #longitude")
    public GeocodingResult reverseGeocode(double latitude, double longitude) {
        if (!config.isConfigured()) {
            log.warn("Google Maps API key is not configured. Using mock response.");
            return createMockReverseResult(latitude, longitude);
        }

        String url = UriComponentsBuilder.fromHttpUrl(GEOCODING_URL)
                .queryParam("latlng", latitude + "," + longitude)
                .queryParam("key", config.getApiKey())
                .queryParam("language", config.getLanguage())
                .build()
                .toUriString();

        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            return parseGeocodingResponse(response);
        } catch (Exception e) {
            log.error("Reverse geocoding failed for coordinates: {}, {}", latitude, longitude, e);
            throw new GeocodingException("좌표 변환 실패: " + latitude + ", " + longitude);
        }
    }

    /**
     * 주소로 좌표만 조회
     */
    public Coordinates getCoordinates(String address) {
        GeocodingResult result = geocode(address);
        return Coordinates.of(result.getLatitude(), result.getLongitude());
    }

    @SuppressWarnings("unchecked")
    private GeocodingResult parseGeocodingResponse(Map<String, Object> response) {
        String status = (String) response.get("status");
        if (!"OK".equals(status)) {
            throw new GeocodingException("Geocoding API error: " + status);
        }

        List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
        if (results == null || results.isEmpty()) {
            throw new GeocodingException("No results found");
        }

        Map<String, Object> firstResult = results.get(0);
        String formattedAddress = (String) firstResult.get("formatted_address");
        String placeId = (String) firstResult.get("place_id");

        Map<String, Object> geometry = (Map<String, Object>) firstResult.get("geometry");
        Map<String, Object> location = (Map<String, Object>) geometry.get("location");
        double lat = ((Number) location.get("lat")).doubleValue();
        double lng = ((Number) location.get("lng")).doubleValue();

        List<Map<String, Object>> addressComponents =
                (List<Map<String, Object>>) firstResult.get("address_components");

        return GeocodingResult.builder()
                .formattedAddress(formattedAddress)
                .latitude(lat)
                .longitude(lng)
                .placeId(placeId)
                .components(parseAddressComponents(addressComponents))
                .build();
    }

    @SuppressWarnings("unchecked")
    private AddressComponents parseAddressComponents(List<Map<String, Object>> components) {
        String country = null;
        String province = null;
        String city = null;
        String district = null;
        String street = null;
        String streetNumber = null;
        String postalCode = null;

        for (Map<String, Object> component : components) {
            List<String> types = (List<String>) component.get("types");
            String longName = (String) component.get("long_name");

            if (types.contains("country")) {
                country = longName;
            } else if (types.contains("administrative_area_level_1")) {
                province = longName;
            } else if (types.contains("locality")) {
                city = longName;
            } else if (types.contains("sublocality_level_1") || types.contains("sublocality")) {
                district = longName;
            } else if (types.contains("route")) {
                street = longName;
            } else if (types.contains("street_number")) {
                streetNumber = longName;
            } else if (types.contains("postal_code")) {
                postalCode = longName;
            }
        }

        return AddressComponents.builder()
                .country(country)
                .province(province)
                .city(city)
                .district(district)
                .street(street)
                .streetNumber(streetNumber)
                .postalCode(postalCode)
                .build();
    }

    private GeocodingResult createMockResult(String address) {
        // 테스트/개발용 목 데이터 (서울 시청 기준)
        return GeocodingResult.builder()
                .formattedAddress(address)
                .latitude(37.5666805)
                .longitude(126.9784147)
                .placeId("mock_place_id")
                .components(AddressComponents.builder()
                        .country("대한민국")
                        .province("서울특별시")
                        .city("중구")
                        .build())
                .build();
    }

    private GeocodingResult createMockReverseResult(double latitude, double longitude) {
        return GeocodingResult.builder()
                .formattedAddress("대한민국 서울특별시")
                .latitude(latitude)
                .longitude(longitude)
                .placeId("mock_place_id")
                .components(AddressComponents.builder()
                        .country("대한민국")
                        .province("서울특별시")
                        .build())
                .build();
    }
}
