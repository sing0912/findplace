package com.petpro.global.config.datasource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DataSourceContextHolder
 *
 * 현재 스레드의 데이터소스 컨텍스트를 관리하는 유틸리티 클래스입니다.
 * ThreadLocal을 사용하여 각 스레드별로 독립적인 데이터소스 타입을 저장하고,
 * 트랜잭션 처리 시 Master/Slave 데이터소스 라우팅에 활용됩니다.
 *
 * 주요 기능:
 * - 스레드별 데이터소스 타입 설정 및 조회
 * - 스레드 안전성 보장 (ThreadLocal 사용)
 * - 기본값으로 MASTER 데이터소스 반환
 */
public class DataSourceContextHolder {

    /** 로깅을 위한 Logger 인스턴스 */
    private static final Logger log = LoggerFactory.getLogger(DataSourceContextHolder.class);

    /** 스레드별 데이터소스 타입을 저장하는 ThreadLocal 변수 */
    private static final ThreadLocal<DataSourceType> contextHolder = new ThreadLocal<>();

    /**
     * 유틸리티 클래스이므로 인스턴스화를 방지하기 위한 private 생성자
     */
    private DataSourceContextHolder() {
        // Utility class
    }

    /**
     * 현재 스레드의 데이터소스 타입을 설정합니다.
     *
     * @param dataSourceType 설정할 데이터소스 타입 (MASTER 또는 SLAVE)
     * @throws IllegalArgumentException dataSourceType이 null인 경우
     */
    public static void setDataSourceType(DataSourceType dataSourceType) {
        if (dataSourceType == null) {
            throw new IllegalArgumentException("DataSourceType cannot be null");
        }
        log.debug("Setting datasource type to: {}", dataSourceType);
        contextHolder.set(dataSourceType);
    }

    /**
     * 현재 스레드의 데이터소스 타입을 반환합니다.
     * 설정된 값이 없으면 기본값으로 MASTER를 반환합니다.
     *
     * @return 현재 데이터소스 타입 (기본값: MASTER)
     */
    public static DataSourceType getDataSourceType() {
        DataSourceType type = contextHolder.get();
        return type != null ? type : DataSourceType.MASTER;
    }

    /**
     * 현재 스레드의 데이터소스 타입을 제거합니다.
     * 트랜잭션 완료 후 메모리 누수를 방지하기 위해 반드시 호출해야 합니다.
     */
    public static void clearDataSourceType() {
        log.debug("Clearing datasource type");
        contextHolder.remove();
    }
}
