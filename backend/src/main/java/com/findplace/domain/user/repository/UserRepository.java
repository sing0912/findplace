package com.findplace.domain.user.repository;

import com.findplace.domain.user.entity.User;
import com.findplace.domain.user.entity.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * 사용자 레포지토리 인터페이스
 *
 * 사용자 데이터베이스 접근을 담당하는 JPA 레포지토리
 * - @Transactional(readOnly = true) 메서드 호출 시 Slave DB로 라우팅
 * - 그 외 메서드는 Master DB로 라우팅
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 이메일로 사용자 조회
     *
     * @param email 이메일
     * @return 사용자 Optional
     */
    Optional<User> findByEmail(String email);

    /**
     * ID로 삭제되지 않은 사용자 조회
     *
     * @param id 사용자 ID
     * @return 사용자 Optional
     */
    Optional<User> findByIdAndDeletedAtIsNull(Long id);

    /**
     * 이메일로 삭제되지 않은 사용자 조회
     *
     * @param email 이메일
     * @return 사용자 Optional
     */
    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    /**
     * 이메일 중복 확인
     *
     * @param email 이메일
     * @return 존재 여부
     */
    boolean existsByEmail(String email);

    /**
     * 전화번호 중복 확인
     *
     * @param phone 전화번호
     * @return 존재 여부
     */
    boolean existsByPhone(String phone);

    /**
     * 상태별 사용자 목록 조회 (삭제된 사용자 제외)
     *
     * @param status 계정 상태
     * @param pageable 페이지네이션 정보
     * @return 사용자 페이지
     */
    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL AND u.status = :status")
    Page<User> findAllByStatus(@Param("status") UserStatus status, Pageable pageable);

    /**
     * 활성 사용자 전체 목록 조회 (삭제된 사용자 제외)
     *
     * @param pageable 페이지네이션 정보
     * @return 사용자 페이지
     */
    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL")
    Page<User> findAllActive(Pageable pageable);

    /**
     * 키워드로 사용자 검색 (이름, 이메일에서 검색)
     *
     * @param keyword 검색 키워드
     * @param pageable 페이지네이션 정보
     * @return 검색 결과 페이지
     */
    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL AND " +
           "(LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<User> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
