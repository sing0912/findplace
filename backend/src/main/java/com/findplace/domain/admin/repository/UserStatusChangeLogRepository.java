package com.findplace.domain.admin.repository;

import com.findplace.domain.admin.entity.UserStatusChangeLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 사용자 상태 변경 이력 레포지토리
 */
@Repository
public interface UserStatusChangeLogRepository extends JpaRepository<UserStatusChangeLog, Long> {

    List<UserStatusChangeLog> findByUserIdOrderByChangedAtDesc(Long userId);

    Page<UserStatusChangeLog> findByUserIdOrderByChangedAtDesc(Long userId, Pageable pageable);
}
