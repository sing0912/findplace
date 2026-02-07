package com.petpro.domain.log.util;

import com.petpro.domain.log.entity.DeviceType;
import jakarta.servlet.http.HttpServletRequest;

/**
 * HTTP 요청 컨텍스트 유틸리티
 *
 * IP 주소, UserAgent, DeviceType 추출
 */
public final class RequestContextUtil {

    private RequestContextUtil() {
    }

    /**
     * 클라이언트 IP 주소 추출
     * X-Forwarded-For 헤더를 우선 확인합니다.
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * UserAgent 헤더 추출
     */
    public static String getUserAgent(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        return request.getHeader("User-Agent");
    }

    /**
     * UserAgent 문자열로 디바이스 유형 판별
     */
    public static DeviceType detectDeviceType(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return DeviceType.OTHER;
        }

        String ua = userAgent.toLowerCase();

        if (ua.contains("tablet") || ua.contains("ipad") || ua.contains("playbook")
                || ua.contains("silk") || (ua.contains("android") && !ua.contains("mobile"))) {
            return DeviceType.TABLET;
        }

        if (ua.contains("mobile") || ua.contains("iphone") || ua.contains("ipod")
                || ua.contains("android") || ua.contains("blackberry")
                || ua.contains("windows phone") || ua.contains("opera mini")) {
            return DeviceType.MOBILE;
        }

        if (ua.contains("mozilla") || ua.contains("chrome") || ua.contains("safari")
                || ua.contains("firefox") || ua.contains("msie") || ua.contains("edge")) {
            return DeviceType.DESKTOP;
        }

        return DeviceType.OTHER;
    }
}
