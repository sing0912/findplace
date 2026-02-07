package com.petpro.domain.auth.service;

/**
 * SMS 발송 서비스 인터페이스.
 * 프로필별 구현체:
 * - MockSmsService (local, test): 콘솔 로그 출력
 * - CoolSmsService (prod): 실제 SMS 발송 (미구현)
 */
public interface SmsService {

    /**
     * 인증번호 SMS 발송
     * @param phone 수신 번호 (01012345678 형식)
     * @param code  6자리 인증번호
     */
    void sendVerificationCode(String phone, String code);
}
