package com.findplace.domain.location.exception;

import com.findplace.global.exception.BusinessException;
import com.findplace.global.exception.ErrorCode;

/**
 * 지오코딩 관련 예외
 */
public class GeocodingException extends BusinessException {

    public GeocodingException() {
        super(ErrorCode.GEOCODING_FAILED);
    }

    public GeocodingException(String message) {
        super(ErrorCode.GEOCODING_FAILED, message);
    }
}
