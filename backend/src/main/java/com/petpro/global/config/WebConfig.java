package com.petpro.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebConfig
 *
 * Spring MVC 웹 설정을 커스터마이즈하기 위한 구성 클래스입니다.
 * WebMvcConfigurer 인터페이스를 구현하여 필요한 설정을 오버라이드할 수 있습니다.
 *
 * 확장 가능한 설정:
 * - 인터셉터 추가
 * - 메시지 컨버터 설정
 * - 리소스 핸들러 등록
 * - 뷰 리졸버 설정
 * - CORS 매핑 등
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    // 추가 웹 설정이 필요한 경우 여기에 구현
}
