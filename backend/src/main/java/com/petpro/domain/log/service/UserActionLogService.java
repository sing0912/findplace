package com.petpro.domain.log.service;

import com.petpro.domain.log.dto.LogRequest.UserActionLogRequest;
import com.petpro.domain.log.entity.DeviceType;
import com.petpro.domain.log.entity.UserActionLog;
import com.petpro.domain.log.repository.UserActionLogRepository;
import com.petpro.domain.log.util.RequestContextUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 행위 로그 서비스
 *
 * 비동기로 로그를 MySQL에 저장합니다.
 * 실패 시 log.error()만 출력하고 예외를 전파하지 않습니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserActionLogService {

    private final UserActionLogRepository userActionLogRepository;

    /**
     * 사용자 행위 로그 비동기 저장
     */
    @Async("logAsyncExecutor")
    @Transactional("logTransactionManager")
    public void logUserAction(UserActionLogRequest request, HttpServletRequest httpRequest) {
        try {
            String ipAddress = RequestContextUtil.getClientIp(httpRequest);
            String userAgent = RequestContextUtil.getUserAgent(httpRequest);
            DeviceType deviceType = RequestContextUtil.detectDeviceType(userAgent);

            UserActionLog logEntry = UserActionLog.create(
                    request.getUserId(),
                    request.getActionType(),
                    request.getTargetType(),
                    request.getTargetId(),
                    request.getDescription(),
                    request.getDetailJson(),
                    ipAddress,
                    userAgent,
                    deviceType
            );

            userActionLogRepository.save(logEntry);
            log.debug("사용자 행위 로그 저장 완료: userId={}, action={}", request.getUserId(), request.getActionType());
        } catch (Exception e) {
            log.error("사용자 행위 로그 저장 실패: userId={}, action={}, error={}",
                    request.getUserId(), request.getActionType(), e.getMessage(), e);
        }
    }

    /**
     * 사용자 행위 로그 비동기 저장 (HttpServletRequest 없이)
     */
    @Async("logAsyncExecutor")
    @Transactional("logTransactionManager")
    public void logUserAction(UserActionLogRequest request) {
        try {
            UserActionLog logEntry = UserActionLog.create(
                    request.getUserId(),
                    request.getActionType(),
                    request.getTargetType(),
                    request.getTargetId(),
                    request.getDescription(),
                    request.getDetailJson(),
                    null,
                    null,
                    null
            );

            userActionLogRepository.save(logEntry);
            log.debug("사용자 행위 로그 저장 완료: userId={}, action={}", request.getUserId(), request.getActionType());
        } catch (Exception e) {
            log.error("사용자 행위 로그 저장 실패: userId={}, action={}, error={}",
                    request.getUserId(), request.getActionType(), e.getMessage(), e);
        }
    }
}
