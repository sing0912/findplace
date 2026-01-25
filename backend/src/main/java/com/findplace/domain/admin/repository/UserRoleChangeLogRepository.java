package com.findplace.domain.admin.repository;

import com.findplace.domain.admin.entity.UserRoleChangeLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 사용자 역할 변경 이력 레포지토리
 */
@Repository
public interface UserRoleChangeLogRepository extends JpaRepository<UserRoleChangeLog, Long> {

    List<UserRoleChangeLog> findByUserIdOrderByChangedAtDesc(Long userId);

    Page<UserRoleChangeLog> findByUserIdOrderByChangedAtDesc(Long userId, Pageable pageable);
}
