/**
 * @fileoverview 알림(토스트) 컴포넌트
 *
 * 사용자에게 피드백 메시지를 표시하는 스낵바 컴포넌트입니다.
 * 성공, 에러, 경고, 정보 등 다양한 유형의 알림을 지원합니다.
 */

import React, { useEffect } from 'react';
import { Snackbar, Alert, AlertColor } from '@mui/material';
import { useUIStore } from '../../stores/uiStore';

/**
 * 알림 컴포넌트
 *
 * UI 스토어의 알림 목록에서 첫 번째 알림을 화면 우상단에 표시합니다.
 * 설정된 시간(기본 5초) 후 자동으로 사라지며, 사용자가 직접 닫을 수도 있습니다.
 *
 * 특징:
 * - 큐 방식: 여러 알림이 있을 경우 순차적으로 표시
 * - 자동 닫기: 설정된 duration 후 자동으로 닫힘
 * - 수동 닫기: 사용자가 닫기 버튼 클릭 가능
 *
 * @example
 * // 사용법 (다른 컴포넌트에서)
 * const { addNotification } = useUIStore();
 * addNotification({ type: 'success', message: '저장되었습니다.' });
 */
const Notification: React.FC = () => {
  const { notifications, removeNotification } = useUIStore();

  /** 현재 표시할 알림 (큐의 첫 번째 항목) */
  const currentNotification = notifications[0];

  /**
   * 알림 자동 제거 타이머 설정
   * 알림이 있을 때 설정된 시간(기본 5초) 후 자동으로 제거됩니다.
   */
  useEffect(() => {
    if (currentNotification) {
      const timer = setTimeout(() => {
        removeNotification(currentNotification.id);
      }, currentNotification.duration || 5000);

      return () => clearTimeout(timer);
    }
  }, [currentNotification, removeNotification]);

  // 표시할 알림이 없으면 렌더링하지 않음
  if (!currentNotification) {
    return null;
  }

  return (
    <Snackbar
      open={!!currentNotification}
      anchorOrigin={{ vertical: 'top', horizontal: 'right' }}
      onClose={() => removeNotification(currentNotification.id)}
    >
      <Alert
        onClose={() => removeNotification(currentNotification.id)}
        severity={currentNotification.type as AlertColor}
        variant="filled"
        sx={{ width: '100%' }}
      >
        {currentNotification.message}
      </Alert>
    </Snackbar>
  );
};

export default Notification;
