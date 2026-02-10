package com.petpro.global.config;

import com.petpro.global.security.jwt.JwtAuthenticationFilter;
import com.petpro.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * SecurityConfig
 *
 * Spring Security 보안 설정을 담당하는 구성 클래스입니다.
 * JWT 기반 인증, CORS 정책, 엔드포인트별 접근 권한 등을 설정합니다.
 *
 * 주요 설정:
 * - Stateless 세션 정책 (JWT 사용으로 서버 세션 미사용)
 * - CSRF 비활성화 (REST API 특성상 불필요)
 * - JWT 인증 필터 적용
 * - 공개/인증 필요 엔드포인트 구분
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    /** JWT 토큰 처리를 위한 프로바이더 */
    private final JwtTokenProvider jwtTokenProvider;
    private final Environment environment;

    /**
     * Spring Security 필터 체인을 구성합니다.
     * HTTP 요청에 대한 보안 규칙을 정의합니다.
     *
     * @param http HttpSecurity 설정 객체
     * @return 구성된 SecurityFilterChain
     * @throws Exception 보안 설정 중 발생한 예외
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000))
                        .contentTypeOptions(contentTypeOptions -> {})
                        .frameOptions(frameOptions -> frameOptions.deny())
                        .xssProtection(xss -> xss
                                .headerValue(org.springframework.security.web.header.writers.XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - 인증 없이 접근 가능한 엔드포인트
                        .requestMatchers("/health", "/actuator/health").permitAll()
                        .requestMatchers("/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        // Auth endpoints - 인증 관련 (로그인, 회원가입, 토큰 갱신, 중복확인, 아이디찾기, 비밀번호재설정, OAuth)
                        .requestMatchers("/v1/auth/**").permitAll()
                        // Public GET endpoints
                        .requestMatchers(HttpMethod.GET, "/v1/funeral-homes/**").permitAll()  // 장례식장 조회는 공개
                        // All other requests require authentication - 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS(Cross-Origin Resource Sharing) 정책을 구성합니다.
     * 허용된 출처, 메서드, 헤더 등을 정의합니다.
     *
     * @return CORS 설정 소스
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        List<String> allowedOrigins = new ArrayList<>();
        allowedOrigins.add("https://*.findplace.co.kr");

        // 프로덕션이 아닌 경우에만 localhost 허용
        boolean isProd = Arrays.asList(environment.getActiveProfiles()).contains("prod");
        if (!isProd) {
            allowedOrigins.add("http://localhost:*");
        }

        configuration.setAllowedOriginPatterns(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin"));
        configuration.setExposedHeaders(List.of("Authorization", "X-Total-Count"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * 비밀번호 인코더를 생성합니다.
     * BCrypt 해시 알고리즘을 사용하여 비밀번호를 암호화합니다.
     *
     * @return BCryptPasswordEncoder 인스턴스
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 인증 관리자를 생성합니다.
     * Spring Security의 인증 처리를 담당합니다.
     *
     * @param authConfig 인증 설정 객체
     * @return AuthenticationManager 인스턴스
     * @throws Exception 인증 관리자 생성 중 발생한 예외
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
