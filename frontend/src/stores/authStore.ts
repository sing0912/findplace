/**
 * @fileoverview 인증 상태 관리 스토어
 *
 * Zustand를 사용하여 사용자 인증 상태를 전역으로 관리합니다.
 * 로그인/로그아웃 상태, 현재 사용자 정보를 저장하며,
 * localStorage에 상태를 영속화하여 페이지 새로고침 시에도 유지됩니다.
 */

import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { User } from '../types/user';

/**
 * 인증 상태 인터페이스
 * 스토어에서 관리하는 상태와 액션들을 정의합니다.
 */
interface AuthState {
  /** 현재 로그인한 사용자 정보 (비로그인 시 null) */
  user: User | null;
  /** 인증 여부 */
  isAuthenticated: boolean;
  /** 인증 상태 로딩 중 여부 */
  isLoading: boolean;

  // Actions
  /**
   * 사용자 정보를 설정합니다.
   * @param user - 설정할 사용자 정보 또는 null
   */
  setUser: (user: User | null) => void;
  /**
   * 로딩 상태를 설정합니다.
   * @param loading - 로딩 상태 값
   */
  setLoading: (loading: boolean) => void;
  /**
   * 로그인 처리를 수행합니다.
   * @param user - 로그인한 사용자 정보
   * @param accessToken - 발급받은 액세스 토큰
   * @param refreshToken - 발급받은 리프레시 토큰
   */
  login: (user: User, accessToken: string, refreshToken: string) => void;
  /**
   * 로그아웃 처리를 수행합니다.
   * 저장된 토큰을 제거하고 상태를 초기화합니다.
   */
  logout: () => void;
}

/**
 * 인증 상태 관리 스토어
 *
 * Zustand의 persist 미들웨어를 사용하여 인증 상태를 localStorage에 저장합니다.
 * 이를 통해 페이지 새로고침 후에도 로그인 상태가 유지됩니다.
 *
 * @example
 * const { user, isAuthenticated, login, logout } = useAuthStore();
 */
export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      user: null,
      isAuthenticated: false,
      isLoading: true,

      setUser: (user) =>
        set({
          user,
          isAuthenticated: !!user,
        }),

      setLoading: (loading) => set({ isLoading: loading }),

      login: (user, accessToken, refreshToken) => {
        localStorage.setItem('accessToken', accessToken);
        localStorage.setItem('refreshToken', refreshToken);
        set({
          user,
          isAuthenticated: true,
          isLoading: false,
        });
      },

      logout: () => {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        set({
          user: null,
          isAuthenticated: false,
          isLoading: false,
        });
      },
    }),
    {
      /** localStorage 저장 키 */
      name: 'auth-storage',
      /** 영속화할 상태 필드 선택 */
      partialize: (state) => ({ user: state.user, isAuthenticated: state.isAuthenticated }),
      /**
       * localStorage에서 상태 복원 완료 후 호출되는 콜백
       * 복원이 완료되면 로딩 상태를 false로 설정합니다.
       */
      onRehydrateStorage: () => (state) => {
        if (state) {
          state.setLoading(false);
        }
      },
    }
  )
);
