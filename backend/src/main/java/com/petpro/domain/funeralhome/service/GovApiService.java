package com.petpro.domain.funeralhome.service;

import com.petpro.domain.funeralhome.dto.GovApiResponse;
import com.petpro.global.exception.BusinessException;
import com.petpro.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 공공데이터포털 동물장묘업 API 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GovApiService {

    private final RestTemplate restTemplate;

    @Value("${app.gov-api.base-url:https://apis.data.go.kr/1741000/animal_cremation}")
    private String baseUrl;

    @Value("${app.gov-api.service-key:}")
    private String serviceKey;

    @Value("${app.gov-api.daily-limit:10000}")
    private int dailyLimit;

    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000;

    /** 일일 API 호출 카운터 */
    private final AtomicInteger dailyCallCount = new AtomicInteger(0);

    /**
     * API 호출 가능 여부 확인
     */
    public boolean canCall() {
        return dailyCallCount.get() < dailyLimit;
    }

    /**
     * 남은 호출 가능 횟수
     */
    public int getRemainingCalls() {
        return dailyLimit - dailyCallCount.get();
    }

    /**
     * 일일 호출 카운트 리셋 (매일 자정 스케줄러에서 호출)
     */
    public void resetDailyCount() {
        int previousCount = dailyCallCount.getAndSet(0);
        log.info("Daily API call count reset. Previous count: {}", previousCount);
    }

    /**
     * 장례식장 데이터 조회 (재시도 지원)
     *
     * @param pageNo 페이지 번호
     * @param numOfRows 페이지당 행 수 (최대 100)
     * @return API 응답
     */
    public GovApiResponse fetchFuneralHomes(int pageNo, int numOfRows) {
        if (!canCall()) {
            log.warn("Daily API call limit reached: {}/{}", dailyCallCount.get(), dailyLimit);
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR);
        }

        if (serviceKey == null || serviceKey.isBlank()) {
            log.warn("GOV API service key is not configured. Returning mock data.");
            return createMockResponse();
        }

        String url = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/getAnimalCremationList")
                .queryParam("serviceKey", serviceKey)
                .queryParam("pageNo", pageNo)
                .queryParam("numOfRows", Math.min(numOfRows, 100))
                .queryParam("type", "json")
                .build()
                .toUriString();

        log.debug("Calling GOV API: page={}, rows={}", pageNo, numOfRows);

        // 재시도 로직
        int attempts = 0;
        RestClientException lastException = null;

        while (attempts < MAX_RETRIES) {
            try {
                ResponseEntity<GovApiResponse> response = restTemplate.getForEntity(url, GovApiResponse.class);
                dailyCallCount.incrementAndGet();

                GovApiResponse body = response.getBody();
                if (body == null || !body.isSuccess()) {
                    String errorCode = body != null && body.getResponse() != null
                            ? body.getResponse().getHeader().getResultCode() : "unknown";
                    log.error("GOV API returned error: code={}", errorCode);
                    throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR);
                }

                log.debug("GOV API response: totalCount={}, items={}",
                        body.getTotalCount(), body.getItems().size());

                return body;
            } catch (RestClientException e) {
                lastException = e;
                attempts++;
                log.warn("GOV API call failed (attempt {}/{}): {}", attempts, MAX_RETRIES, e.getMessage());

                if (attempts < MAX_RETRIES) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS * attempts); // 지수 백오프
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        log.error("GOV API call failed after {} retries", MAX_RETRIES, lastException);
        throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR);
    }

    /**
     * 전체 데이터 페이지 수 조회
     */
    public int getTotalPages(int numOfRows) {
        GovApiResponse response = fetchFuneralHomes(1, 1);
        int totalCount = response.getTotalCount();
        return (int) Math.ceil((double) totalCount / numOfRows);
    }

    /**
     * 목업 응답 생성 (API 키 미설정 시)
     */
    private GovApiResponse createMockResponse() {
        log.info("Returning mock GOV API response for development");
        return new GovApiResponse();
    }
}
