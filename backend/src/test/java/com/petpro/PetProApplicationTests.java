package com.petpro;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Disabled("통합 테스트 환경 설정 후 활성화")
class PetProApplicationTests {

    @Test
    void contextLoads() {
    }
}
