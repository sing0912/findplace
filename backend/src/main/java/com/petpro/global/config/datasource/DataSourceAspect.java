package com.petpro.global.config.datasource;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;

/**
 * DataSourceAspect
 *
 * @Transactional 어노테이션의 readOnly 속성에 따라 데이터소스를 자동으로 라우팅하는 AOP 클래스입니다.
 * 트랜잭션 시작 전에 가장 먼저 실행되도록 HIGHEST_PRECEDENCE로 우선순위가 설정되어 있습니다.
 *
 * 라우팅 규칙:
 * - readOnly = true: Slave 데이터소스 사용 (조회 작업)
 * - readOnly = false (기본값): Master 데이터소스 사용 (CUD 작업)
 */
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DataSourceAspect {

    /** 로깅을 위한 Logger 인스턴스 */
    private static final Logger log = LoggerFactory.getLogger(DataSourceAspect.class);

    /**
     * @Transactional 어노테이션이 적용된 메서드를 가로채어 데이터소스 타입을 결정합니다.
     * 메서드 레벨의 어노테이션이 없으면 클래스 레벨의 어노테이션을 확인합니다.
     *
     * @param joinPoint AOP 조인 포인트 (대상 메서드 정보 포함)
     * @return 대상 메서드의 실행 결과
     * @throws Throwable 대상 메서드 실행 중 발생한 예외
     */
    @Around("@annotation(org.springframework.transaction.annotation.Transactional)")
    public Object determineDataSource(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Transactional transactional = method.getAnnotation(Transactional.class);

        if (transactional == null) {
            transactional = joinPoint.getTarget().getClass().getAnnotation(Transactional.class);
        }

        DataSourceType dataSourceType = DataSourceType.MASTER;
        if (transactional != null && transactional.readOnly()) {
            dataSourceType = DataSourceType.SLAVE;
        }

        log.debug("Method: {}, DataSource: {}", method.getName(), dataSourceType);
        DataSourceContextHolder.setDataSourceType(dataSourceType);

        try {
            return joinPoint.proceed();
        } finally {
            DataSourceContextHolder.clearDataSourceType();
        }
    }
}
