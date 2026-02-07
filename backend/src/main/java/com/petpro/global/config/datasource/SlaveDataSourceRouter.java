package com.petpro.global.config.datasource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * SlaveDataSourceRouter
 *
 * 여러 Slave 데이터소스 간의 부하 분산을 위한 Round Robin 라우터입니다.
 * 읽기 전용 쿼리를 여러 Slave 노드에 균등하게 분배하여 부하를 분산시킵니다.
 *
 * 동작 방식:
 * - AtomicInteger 카운터를 사용하여 스레드 안전한 Round Robin 구현
 * - 요청마다 다음 Slave 데이터소스를 순차적으로 반환
 */
public class SlaveDataSourceRouter {

    /** 로깅을 위한 Logger 인스턴스 */
    private static final Logger log = LoggerFactory.getLogger(SlaveDataSourceRouter.class);

    /** Slave 데이터소스 목록 */
    private final List<DataSource> slaveDataSources;

    /** Round Robin을 위한 원자적 카운터 */
    private final AtomicInteger counter = new AtomicInteger(0);

    /**
     * SlaveDataSourceRouter 생성자
     *
     * @param slaveDataSources Slave 데이터소스 목록 (최소 1개 이상 필수)
     * @throws IllegalArgumentException slaveDataSources가 null이거나 비어있는 경우
     */
    public SlaveDataSourceRouter(List<DataSource> slaveDataSources) {
        if (slaveDataSources == null || slaveDataSources.isEmpty()) {
            throw new IllegalArgumentException("Slave DataSources cannot be null or empty");
        }
        this.slaveDataSources = slaveDataSources;
    }

    /**
     * Round Robin 방식으로 다음 Slave 데이터소스를 반환합니다.
     * 카운터 오버플로우 시에도 음수 인덱스를 방지하기 위해 Math.abs를 사용합니다.
     *
     * @return 다음 순서의 Slave 데이터소스
     */
    public DataSource getNextSlaveDataSource() {
        int index = Math.abs(counter.getAndIncrement() % slaveDataSources.size());
        log.debug("Routing to slave index: {}", index);
        return slaveDataSources.get(index);
    }

    /**
     * 등록된 모든 Slave 데이터소스 목록을 반환합니다.
     *
     * @return Slave 데이터소스 목록
     */
    public List<DataSource> getAllSlaveDataSources() {
        return slaveDataSources;
    }
}
