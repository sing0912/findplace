package com.petpro.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * RedisConfig
 *
 * Redis 캐시 서버 연결 및 캐시 관리자 설정 클래스입니다.
 * Lettuce 클라이언트를 사용하여 Redis에 연결하고, 캐시 전략을 정의합니다.
 *
 * 주요 기능:
 * - Redis 연결 팩토리 구성 (Lettuce 사용)
 * - RedisTemplate 설정 (JSON 직렬화)
 * - 캐시 관리자 구성 (도메인별 TTL 설정)
 */
@Configuration
@EnableCaching
public class RedisConfig {

    /** Redis 서버 호스트 주소 */
    @Value("${spring.data.redis.host}")
    private String host;

    /** Redis 서버 포트 번호 */
    @Value("${spring.data.redis.port}")
    private int port;

    /** Redis 서버 비밀번호 */
    @Value("${spring.data.redis.password}")
    private String password;

    /**
     * Redis 연결 팩토리를 생성합니다.
     * Lettuce 클라이언트를 사용하여 비동기/논블로킹 방식으로 Redis에 연결합니다.
     * 비밀번호 인증을 포함하여 안전한 연결을 보장합니다.
     *
     * @return LettuceConnectionFactory 인스턴스
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName(host);
        redisConfig.setPort(port);
        redisConfig.setPassword(password);
        return new LettuceConnectionFactory(redisConfig);
    }

    /**
     * Redis 작업을 위한 템플릿을 생성합니다.
     * Key는 String, Value는 JSON 형식으로 직렬화됩니다.
     *
     * @param connectionFactory Redis 연결 팩토리
     * @return RedisTemplate 인스턴스
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }

    /**
     * 캐시 관리자를 생성합니다.
     * 도메인별로 다른 TTL(Time To Live) 정책을 적용합니다.
     *
     * 캐시 TTL 설정:
     * - 기본: 30분
     * - companies: 1시간 (업체 정보는 자주 변경되지 않음)
     * - products: 15분 (상품 정보는 비교적 자주 변경됨)
     * - users: 30분 (사용자 정보는 중간 빈도로 변경됨)
     *
     * @param connectionFactory Redis 연결 팩토리
     * @return RedisCacheManager 인스턴스
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .withCacheConfiguration("companies", RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(1)))
                .withCacheConfiguration("products", RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(15)))
                .withCacheConfiguration("users", RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(30)))
                .build();
    }
}
