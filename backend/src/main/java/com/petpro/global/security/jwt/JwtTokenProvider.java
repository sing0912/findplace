package com.petpro.global.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JwtTokenProvider
 *
 * JWT(JSON Web Token) 생성, 검증, 파싱을 담당하는 컴포넌트입니다.
 * Access Token과 Refresh Token을 관리하며, 토큰 기반 인증의 핵심 기능을 제공합니다.
 *
 * 주요 기능:
 * - Access Token 생성 (사용자 인증용, 짧은 만료 시간)
 * - Refresh Token 생성 (토큰 갱신용, 긴 만료 시간)
 * - 토큰에서 인증 정보 추출
 * - 토큰 유효성 검증
 * - HTTP 요청에서 토큰 추출
 */
@Component
public class JwtTokenProvider {

    /** 로깅을 위한 Logger 인스턴스 */
    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    /** Authorization 헤더 이름 */
    private static final String AUTHORIZATION_HEADER = "Authorization";

    /** Bearer 토큰 접두사 */
    private static final String BEARER_PREFIX = "Bearer ";

    /** 역할 정보를 저장하는 클레임 이름 */
    private static final String ROLES_CLAIM = "roles";

    /** JWT 서명에 사용할 비밀 키 문자열 (설정 파일에서 주입) */
    @Value("${app.jwt.secret}")
    private String secretKeyString;

    /** Access Token 만료 시간 (초 단위) */
    @Value("${app.jwt.access-expiration}")
    private long accessTokenExpiration;

    /** Refresh Token 만료 시간 (초 단위) */
    @Value("${app.jwt.refresh-expiration}")
    private long refreshTokenExpiration;

    /** HMAC-SHA 알고리즘에 사용될 SecretKey 객체 */
    private SecretKey secretKey;

    /**
     * 빈 초기화 시 비밀 키 문자열을 SecretKey 객체로 변환합니다.
     */
    @PostConstruct
    protected void init() {
        this.secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Access Token을 생성합니다.
     * 사용자 ID, 이메일, 역할 정보를 포함하며, 짧은 만료 시간을 가집니다.
     *
     * @param userId 사용자 ID
     * @param email 사용자 이메일
     * @param roles 사용자 역할 목록
     * @return 생성된 Access Token 문자열
     */
    public String createAccessToken(Long userId, String email, List<String> roles) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpiration * 1000);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .claim(ROLES_CLAIM, roles)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Refresh Token을 생성합니다.
     * 사용자 ID만 포함하며, Access Token보다 긴 만료 시간을 가집니다.
     *
     * @param userId 사용자 ID
     * @return 생성된 Refresh Token 문자열
     */
    public String createRefreshToken(Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpiration * 1000);

        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    /**
     * 토큰에서 Spring Security Authentication 객체를 추출합니다.
     * 토큰의 클레임에서 사용자 정보와 권한을 읽어 인증 객체를 구성합니다.
     *
     * @param token JWT 토큰 문자열
     * @return Authentication 객체
     */
    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);

        List<String> roles = claims.get(ROLES_CLAIM, List.class);
        List<SimpleGrantedAuthority> authorities = roles != null
                ? roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList())
                : List.of();

        UserDetails principal = User.builder()
                .username(claims.getSubject())
                .password("")
                .authorities(authorities)
                .build();

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    /**
     * 토큰에서 사용자 ID를 추출합니다.
     *
     * @param token JWT 토큰 문자열
     * @return 사용자 ID
     */
    public Long getUserId(String token) {
        return Long.parseLong(parseClaims(token).getSubject());
    }

    /**
     * HTTP 요청의 Authorization 헤더에서 Bearer 토큰을 추출합니다.
     *
     * @param request HTTP 요청 객체
     * @return 토큰 문자열 (Bearer 접두사 제외), 없으면 null
     */
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    /**
     * 토큰의 유효성을 검증합니다.
     * 서명, 만료 시간, 형식 등을 확인합니다.
     *
     * @param token JWT 토큰 문자열
     * @return 유효하면 true, 그렇지 않으면 false
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.warn("Invalid JWT signature: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 토큰을 파싱하여 클레임 정보를 추출합니다.
     *
     * @param token JWT 토큰 문자열
     * @return 토큰의 클레임 정보
     */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
