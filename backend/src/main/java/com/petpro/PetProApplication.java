package com.petpro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * PetPro 애플리케이션 메인 클래스
 *
 * 반려동물 장례 토탈 플랫폼의 Spring Boot 진입점
 * - JPA Auditing은 JpaAuditingConfig에서 활성화
 * - 컴포넌트 스캔 자동 설정
 */
@SpringBootApplication
public class PetProApplication {

    /**
     * 애플리케이션 시작점
     *
     * @param args 명령줄 인수
     */
    public static void main(String[] args) {
        SpringApplication.run(PetProApplication.class, args);
    }
}
