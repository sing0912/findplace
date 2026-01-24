/**
 * @fileoverview UI 상태 관리 스토어
 *
 * Zustand를 사용하여 애플리케이션의 UI 상태를 전역으로 관리합니다.
 * 사이드바 열림/닫힘, 다크 모드, 알림 메시지, 전역 로딩 상태 등을 포함합니다.
 */

import { create } from 'zustand';

/**
 * 알림 메시지 인터페이스
 * 사용자에게 표시되는 토스트 알림 정보입니다.
 */
interface Notification {
  /** 알림 고유 식별자 */
  id: string;
  /** 알림 유형 (성공, 에러, 경고, 정보) */
  type: 'success' | 'error' | 'warning' | 'info';
  /** 알림 메시지 내용 */
  message: string;
  /** 알림 표시 시간 (밀리초, 기본값: 5000) */
  duration?: number;
}

/**
 * UI 상태 인터페이스
 * 스토어에서 관리하는 UI 상태와 액션들을 정의합니다.
 */
interface UIState {
  // 사이드바 관련
  /** 사이드바 열림 상태 */
  sidebarOpen: boolean;
  /** 사이드바 열림/닫힘 토글 */
  toggleSidebar: () => void;
  /**
   * 사이드바 열림 상태 직접 설정
   * @param open - 열림 상태 값
   */
  setSidebarOpen: (open: boolean) => void;

  // 다크 모드 관련
  /** 다크 모드 활성화 여부 */
  darkMode: boolean;
  /** 다크 모드 토글 */
  toggleDarkMode: () => void;

  // 알림 관련
  /** 현재 표시 중인 알림 목록 */
  notifications: Notification[];
  /**
   * 새 알림 추가
   * @param notification - 추가할 알림 정보 (id 제외)
   */
  addNotification: (notification: Omit<Notification, 'id'>) => void;
  /**
   * 알림 제거
   * @param id - 제거할 알림의 id
   */
  removeNotification: (id: string) => void;

  // 로딩 관련
  /** 전역 로딩 상태 */
  globalLoading: boolean;
  /**
   * 전역 로딩 상태 설정
   * @param loading - 로딩 상태 값
   */
  setGlobalLoading: (loading: boolean) => void;
}

/**
 * UI 상태 관리 스토어
 *
 * 애플리케이션 전체에서 공유되는 UI 상태를 관리합니다.
 * 사이드바, 다크 모드, 알림, 로딩 상태 등을 포함합니다.
 *
 * @example
 * const { darkMode, toggleDarkMode, addNotification } = useUIStore();
 * addNotification({ type: 'success', message: '저장되었습니다.' });
 */
export const useUIStore = create<UIState>((set) => ({
  // 사이드바 기본 상태 및 액션
  sidebarOpen: true,
  toggleSidebar: () => set((state) => ({ sidebarOpen: !state.sidebarOpen })),
  setSidebarOpen: (open) => set({ sidebarOpen: open }),

  // 다크 모드 기본 상태 및 액션
  darkMode: false,
  toggleDarkMode: () => set((state) => ({ darkMode: !state.darkMode })),

  // 알림 기본 상태 및 액션
  notifications: [],
  addNotification: (notification) =>
    set((state) => ({
      notifications: [
        ...state.notifications,
        { ...notification, id: Date.now().toString() },
      ],
    })),
  removeNotification: (id) =>
    set((state) => ({
      notifications: state.notifications.filter((n) => n.id !== id),
    })),

  // 전역 로딩 기본 상태 및 액션
  globalLoading: false,
  setGlobalLoading: (loading) => set({ globalLoading: loading }),
}));
