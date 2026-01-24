package com.findplace;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * FindPlace 애플리케이션 메인 클래스
 *
 * 반려동물 장례 토탈 플랫폼의 Spring Boot 진입점
 * - JPA Auditing 활성화 (생성일시, 수정일시 자동 관리)
 * - 컴포넌트 스캔 자동 설정
 */
@SpringBootApplication
@EnableJpaAuditing
public class FindPlaceApplication {

    /**
     * 애플리케이션 시작점
     *
     * @param args 명령줄 인수
     */
    public static void main(String[] args) {
        SpringApplication.run(FindPlaceApplication.class, args);
    }
}
