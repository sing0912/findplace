package com.findplace.global.config.datasource;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DataSourceConfig
 *
 * Master-Slave 데이터베이스 구조를 위한 데이터소스 설정 클래스입니다.
 * 쓰기 작업은 Master로, 읽기 작업은 Slave로 자동 라우팅되도록 구성합니다.
 *
 * 구성 요소:
 * - Master 데이터소스: CUD 작업용 (1개)
 * - Slave 데이터소스: R 작업용 (2개, Round Robin 방식)
 * - LazyConnectionDataSourceProxy: 실제 커넥션 획득을 지연시켜 효율성 향상
 */
@Configuration
public class DataSourceConfig {

    /**
     * Master 데이터소스를 생성합니다.
     * application.yml의 datasource.master 설정을 바인딩합니다.
     *
     * @return Master용 HikariDataSource
     */
    @Bean
    @ConfigurationProperties(prefix = "datasource.master")
    public DataSource masterDataSource() {
        return new HikariDataSource();
    }

    /**
     * 첫 번째 Slave 데이터소스를 생성합니다.
     * application.yml의 datasource.slave.nodes[0] 설정을 바인딩합니다.
     *
     * @return 첫 번째 Slave용 HikariDataSource
     */
    @Bean
    @ConfigurationProperties(prefix = "datasource.slave.nodes[0]")
    public DataSource slave1DataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setPoolName("slave1-pool");
        return dataSource;
    }

    /**
     * 두 번째 Slave 데이터소스를 생성합니다.
     * application.yml의 datasource.slave.nodes[1] 설정을 바인딩합니다.
     *
     * @return 두 번째 Slave용 HikariDataSource
     */
    @Bean
    @ConfigurationProperties(prefix = "datasource.slave.nodes[1]")
    public DataSource slave2DataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setPoolName("slave2-pool");
        return dataSource;
    }

    /**
     * Slave 데이터소스 라우터를 생성합니다.
     * 여러 Slave 노드 간의 부하 분산을 담당합니다.
     *
     * @param slave1 첫 번째 Slave 데이터소스
     * @param slave2 두 번째 Slave 데이터소스
     * @return SlaveDataSourceRouter 인스턴스
     */
    @Bean
    public SlaveDataSourceRouter slaveDataSourceRouter(
            @Qualifier("slave1DataSource") DataSource slave1,
            @Qualifier("slave2DataSource") DataSource slave2) {
        return new SlaveDataSourceRouter(List.of(slave1, slave2));
    }

    /**
     * 라우팅 데이터소스를 생성합니다.
     * 트랜잭션의 readOnly 속성에 따라 Master 또는 Slave로 라우팅합니다.
     *
     * @param masterDataSource Master 데이터소스
     * @param slave1DataSource 첫 번째 Slave 데이터소스
     * @param slave2DataSource 두 번째 Slave 데이터소스
     * @return RoutingDataSource 인스턴스
     */
    @Bean
    public DataSource routingDataSource(
            @Qualifier("masterDataSource") DataSource masterDataSource,
            @Qualifier("slave1DataSource") DataSource slave1DataSource,
            @Qualifier("slave2DataSource") DataSource slave2DataSource) {

        RoutingDataSource routingDataSource = new RoutingDataSource();

        Map<Object, Object> dataSourceMap = new HashMap<>();
        dataSourceMap.put(DataSourceType.MASTER, masterDataSource);
        // Slave는 Round Robin으로 선택되므로 기본값으로 slave1 설정
        dataSourceMap.put(DataSourceType.SLAVE, slave1DataSource);

        routingDataSource.setTargetDataSources(dataSourceMap);
        routingDataSource.setDefaultTargetDataSource(masterDataSource);

        return routingDataSource;
    }

    /**
     * 기본 데이터소스를 생성합니다.
     * LazyConnectionDataSourceProxy를 사용하여 실제 커넥션 획득을 지연시킵니다.
     * 이를 통해 트랜잭션 시작 전까지 커넥션 풀에서 커넥션을 가져오지 않습니다.
     *
     * @param routingDataSource 라우팅 데이터소스
     * @return LazyConnectionDataSourceProxy로 감싼 데이터소스
     */
    @Bean
    @Primary
    public DataSource dataSource(@Qualifier("routingDataSource") DataSource routingDataSource) {
        return new LazyConnectionDataSourceProxy(routingDataSource);
    }
}
