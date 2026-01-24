package com.findplace.global.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JwtAuthenticationFilter
 *
 * HTTP 요청에서 JWT 토큰을 추출하고 인증을 처리하는 필터입니다.
 * OncePerRequestFilter를 상속받아 요청당 한 번만 실행됩니다.
 *
 * 동작 흐름:
 * 1. Authorization 헤더에서 Bearer 토큰 추출
 * 2. 토큰 유효성 검증
 * 3. 유효한 경우 SecurityContext에 인증 정보 설정
 * 4. 필터 체인 계속 진행
 */
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /** 로깅을 위한 Logger 인스턴스 */
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    /** JWT 토큰 처리를 위한 프로바이더 */
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 실제 필터 로직을 수행합니다.
     * 요청에서 JWT 토큰을 추출하고, 유효한 경우 인증 정보를 SecurityContext에 설정합니다.
     *
     * @param request HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @param filterChain 필터 체인
     * @throws ServletException 서블릿 예외
     * @throws IOException 입출력 예외
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = jwtTokenProvider.resolveToken(request);

        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            Authentication authentication = jwtTokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("Set Authentication to security context for '{}', uri: {}", authentication.getName(), request.getRequestURI());
        }

        filterChain.doFilter(request, response);
    }
}
