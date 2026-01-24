package com.findplace.global.config.datasource;

/**
 * DataSourceType
 *
 * 데이터베이스 Master-Slave 구조에서 사용되는 데이터소스 타입을 정의하는 열거형입니다.
 * 트랜잭션의 특성에 따라 적절한 데이터소스로 라우팅하는 데 사용됩니다.
 *
 * 사용 구분:
 * - MASTER: CUD (Create, Update, Delete) 작업용 - 쓰기 작업 전용
 * - SLAVE: R (Read) 작업용 - 읽기 전용 쿼리 전용
 */
public enum DataSourceType {
    /** 쓰기 작업(Create, Update, Delete)을 위한 Master 데이터소스 */
    MASTER,

    /** 읽기 작업(Read)을 위한 Slave 데이터소스 */
    SLAVE
}
