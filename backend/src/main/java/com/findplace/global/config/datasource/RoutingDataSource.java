package com.findplace.global.config.datasource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * RoutingDataSource
 *
 * Spring의 AbstractRoutingDataSource를 상속받아 Master-Slave 데이터소스 라우팅을 구현한 클래스입니다.
 * 현재 스레드의 컨텍스트에 설정된 데이터소스 타입에 따라 적절한 데이터소스를 선택합니다.
 *
 * 라우팅 규칙:
 * - @Transactional(readOnly = true) -> Slave 데이터소스
 * - @Transactional (기본값) -> Master 데이터소스
 */
public class RoutingDataSource extends AbstractRoutingDataSource {

    /**
     * 현재 요청에서 사용할 데이터소스의 Lookup Key를 결정합니다.
     * DataSourceContextHolder에서 현재 스레드의 데이터소스 타입을 조회하여 반환합니다.
     *
     * @return 현재 데이터소스 타입 (MASTER 또는 SLAVE)
     */
    @Override
    protected Object determineCurrentLookupKey() {
        return DataSourceContextHolder.getDataSourceType();
    }
}
