package com.petpro.domain.funeralhome.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FuneralHomeSyncLog 엔티티 테스트")
class FuneralHomeSyncLogTest {

    @Test
    @DisplayName("동기화 시작 로그 생성")
    void start_Incremental() {
        // when
        FuneralHomeSyncLog log = FuneralHomeSyncLog.start(SyncType.INCREMENTAL);

        // then
        assertThat(log.getSyncType()).isEqualTo(SyncType.INCREMENTAL);
        assertThat(log.getStatus()).isEqualTo(SyncStatus.RUNNING);
        assertThat(log.getStartedAt()).isNotNull();
        assertThat(log.getCompletedAt()).isNull();
    }

    @Test
    @DisplayName("동기화 완료 처리")
    void complete_Success() {
        // given
        FuneralHomeSyncLog log = FuneralHomeSyncLog.start(SyncType.FULL);

        // when
        log.complete(100, 50, 30, 20);

        // then
        assertThat(log.getStatus()).isEqualTo(SyncStatus.COMPLETED);
        assertThat(log.getCompletedAt()).isNotNull();
        assertThat(log.getTotalCount()).isEqualTo(100);
        assertThat(log.getInsertedCount()).isEqualTo(50);
        assertThat(log.getUpdatedCount()).isEqualTo(30);
        assertThat(log.getDeletedCount()).isEqualTo(20);
    }

    @Test
    @DisplayName("동기화 부분 완료 처리")
    void partial_WithErrors() {
        // given
        FuneralHomeSyncLog log = FuneralHomeSyncLog.start(SyncType.INCREMENTAL);

        // when
        log.partial(100, 40, 30, 20, 10, "일부 항목 처리 실패");

        // then
        assertThat(log.getStatus()).isEqualTo(SyncStatus.PARTIAL);
        assertThat(log.getCompletedAt()).isNotNull();
        assertThat(log.getTotalCount()).isEqualTo(100);
        assertThat(log.getErrorCount()).isEqualTo(10);
        assertThat(log.getErrorMessage()).isEqualTo("일부 항목 처리 실패");
    }

    @Test
    @DisplayName("동기화 실패 처리")
    void fail_WithError() {
        // given
        FuneralHomeSyncLog log = FuneralHomeSyncLog.start(SyncType.FULL);

        // when
        log.fail("API 호출 실패");

        // then
        assertThat(log.getStatus()).isEqualTo(SyncStatus.FAILED);
        assertThat(log.getCompletedAt()).isNotNull();
        assertThat(log.getErrorMessage()).isEqualTo("API 호출 실패");
    }
}
