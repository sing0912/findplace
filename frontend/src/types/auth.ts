/**
 * @fileoverview 인증 관련 타입 정의
 *
 * 이 파일은 로그인, 회원가입, 토큰 관리 등 인증 프로세스에서
 * 사용되는 요청/응답 타입들을 정의합니다.
 */

/**
 * 로그인 요청 인터페이스
 * 사용자가 로그인할 때 제출하는 정보입니다.
 */
export interface LoginRequest {
  /** 이메일 주소 (로그인 아이디) */
  email: string;
  /** 비밀번호 */
  password: string;
}

/**
 * 회원가입 요청 인터페이스
 * 새 계정을 생성할 때 제출하는 정보입니다.
 */
export interface RegisterRequest {
  /** 이메일 주소 (로그인 아이디로 사용) */
  email: string;
  /** 비밀번호 */
  password: string;
  /** 사용자 이름 */
  name: string;
  /** 전화번호 (선택적) */
  phone?: string;
}

/**
 * 토큰 응답 인터페이스
 * 인증 성공 시 서버에서 반환하는 토큰 정보입니다.
 */
export interface TokenResponse {
  /** 액세스 토큰 (API 요청 인증에 사용) */
  accessToken: string;
  /** 리프레시 토큰 (액세스 토큰 갱신에 사용) */
  refreshToken: string;
  /** 액세스 토큰 만료 시간 (초 단위) */
  expiresIn: number;
  /** 토큰 타입 (일반적으로 "Bearer") */
  tokenType: string;
}

/**
 * 토큰 갱신 요청 인터페이스
 * 만료된 액세스 토큰을 갱신할 때 사용됩니다.
 */
export interface RefreshTokenRequest {
  /** 리프레시 토큰 */
  refreshToken: string;
}
