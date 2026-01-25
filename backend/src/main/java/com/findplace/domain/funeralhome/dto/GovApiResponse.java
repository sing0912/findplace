package com.findplace.domain.funeralhome.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 공공데이터포털 동물장묘업 API 응답 DTO
 */
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GovApiResponse {

    private Response response;

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response {
        private Header header;
        private Body body;
    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Header {
        private String resultCode;
        private String resultMsg;
    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Body {
        private Items items;
        private Integer numOfRows;
        private Integer pageNo;
        private Integer totalCount;
    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Items {
        private List<Item> item;
    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {
        /** 지역 코드 */
        private String locCode;

        /** 지역명 */
        private String locName;

        /** 화장장 여부 (Y/N) */
        private String crematorium;

        /** 납골당 여부 (Y/N) */
        private String columbarium;

        /** 장례식장 여부 (Y/N) */
        private String funeral;

        /** 업소명 */
        private String nm;

        /** 도로명 주소 */
        private String roadAddr;

        /** 지번 주소 */
        private String lotAddr;

        /** 전화번호 */
        private String telno;

        /**
         * 화장장 보유 여부
         */
        public boolean hasCrematorium() {
            return "Y".equalsIgnoreCase(crematorium);
        }

        /**
         * 납골당 보유 여부
         */
        public boolean hasColumbarium() {
            return "Y".equalsIgnoreCase(columbarium);
        }

        /**
         * 장례식장 보유 여부
         */
        public boolean hasFuneral() {
            return "Y".equalsIgnoreCase(funeral);
        }
    }

    /**
     * 응답 성공 여부 확인
     */
    public boolean isSuccess() {
        return response != null
                && response.getHeader() != null
                && "00".equals(response.getHeader().getResultCode());
    }

    /**
     * 응답 아이템 목록 조회
     */
    public List<Item> getItems() {
        if (response != null && response.getBody() != null
                && response.getBody().getItems() != null) {
            return response.getBody().getItems().getItem();
        }
        return List.of();
    }

    /**
     * 전체 건수 조회
     */
    public int getTotalCount() {
        if (response != null && response.getBody() != null) {
            return response.getBody().getTotalCount() != null
                    ? response.getBody().getTotalCount() : 0;
        }
        return 0;
    }
}
