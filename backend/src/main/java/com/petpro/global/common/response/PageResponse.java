package com.petpro.global.common.response;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * PageResponse
 *
 * 페이지네이션된 데이터를 위한 표준 응답 포맷 클래스입니다.
 * Spring Data의 Page 객체를 클라이언트 친화적인 형식으로 변환합니다.
 *
 * 응답 구조:
 * - content: 현재 페이지의 데이터 목록
 * - page: 페이지 메타데이터 (현재 페이지, 크기, 전체 개수 등)
 *
 * @param <T> 페이지 내 데이터의 타입
 */
@Getter
@Builder
public class PageResponse<T> {

    /** 현재 페이지의 데이터 목록 */
    private final List<T> content;

    /** 페이지 메타데이터 */
    private final PageInfo page;

    /**
     * Spring Data Page 객체를 PageResponse로 변환합니다.
     *
     * @param page Spring Data Page 객체
     * @param <T> 데이터 타입
     * @return PageResponse 객체
     */
    public static <T> PageResponse<T> of(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .page(PageInfo.builder()
                        .number(page.getNumber())
                        .size(page.getSize())
                        .totalElements(page.getTotalElements())
                        .totalPages(page.getTotalPages())
                        .first(page.isFirst())
                        .last(page.isLast())
                        .build())
                .build();
    }

    /**
     * PageInfo
     *
     * 페이지네이션 메타데이터를 담는 내부 클래스입니다.
     */
    @Getter
    @Builder
    public static class PageInfo {
        /** 현재 페이지 번호 (0부터 시작) */
        private final int number;

        /** 페이지당 데이터 개수 */
        private final int size;

        /** 전체 데이터 개수 */
        private final long totalElements;

        /** 전체 페이지 수 */
        private final int totalPages;

        /** 첫 번째 페이지 여부 */
        private final boolean first;

        /** 마지막 페이지 여부 */
        private final boolean last;
    }
}
