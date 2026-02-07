package com.petpro.domain.log.service;

import com.petpro.domain.log.dto.LogRequest.AdminActionLogRequest;
import com.petpro.domain.log.entity.AdminActionLog;
import com.petpro.domain.log.repository.AdminActionLogRepository;
import com.petpro.domain.log.util.RequestContextUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 운영자 행위 로그 서비스
 *
 * 비동기로 로그를 MySQL에 저장합니다.
 * 실패 시 log.error()만 출력하고 예외를 전파하지 않습니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminActionLogService {

    private final AdminActionLogRepository adminActionLogRepository;

    /**
     * 운영자 행위 로그 비동기 저장
     */
    @Async("logAsyncExecutor")
    @Transactional("logTransactionManager")
    public void logAdminAction(AdminActionLogRequest request, HttpServletRequest httpRequest) {
        try {
            String ipAddress = RequestContextUtil.getClientIp(httpRequest);
            String userAgent = RequestContextUtil.getUserAgent(httpRequest);

            AdminActionLog logEntry = AdminActionLog.create(
                    request.getAdminId(),
                    request.getActionType(),
                    request.getTargetType(),
                    request.getTargetId(),
                    request.getDescription(),
                    request.getDetailJson(),
                    ipAddress,
                    userAgent
            );

            adminActionLogRepository.save(logEntry);
            log.debug("운영자 행위 로그 저장 완료: adminId={}, action={}", request.getAdminId(), request.getActionType());
        } catch (Exception e) {
            log.error("운영자 행위 로그 저장 실패: adminId={}, action={}, error={}",
                    request.getAdminId(), request.getActionType(), e.getMessage(), e);
        }
    }

    /**
     * 운영자 행위 로그 비동기 저장 (HttpServletRequest 없이)
     */
    @Async("logAsyncExecutor")
    @Transactional("logTransactionManager")
    public void logAdminAction(AdminActionLogRequest request) {
        try {
            AdminActionLog logEntry = AdminActionLog.create(
                    request.getAdminId(),
                    request.getActionType(),
                    request.getTargetType(),
                    request.getTargetId(),
                    request.getDescription(),
                    request.getDetailJson(),
                    null,
                    null
            );

            adminActionLogRepository.save(logEntry);
            log.debug("운영자 행위 로그 저장 완료: adminId={}, action={}", request.getAdminId(), request.getActionType());
        } catch (Exception e) {
            log.error("운영자 행위 로그 저장 실패: adminId={}, action={}, error={}",
                    request.getAdminId(), request.getActionType(), e.getMessage(), e);
        }
    }
}
