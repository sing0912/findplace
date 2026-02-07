package com.petpro.domain.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * SMS Mock 구현체 (local, test 환경).
 * 실제 SMS를 발송하지 않고 콘솔에 인증번호를 출력한다.
 */
@Slf4j
@Service
@Profile({"local", "test"})
public class MockSmsService implements SmsService {

    @Override
    public void sendVerificationCode(String phone, String code) {
        log.info("══════════════════════════════════════");
        log.info("  [SMS MOCK] {} -> 인증번호: {}", phone, code);
        log.info("══════════════════════════════════════");
    }
}
