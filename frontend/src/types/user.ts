/**
 * @fileoverview 사용자 관련 타입 정의
 *
 * 이 파일은 사용자 정보 및 관련 요청/응답에 사용되는 타입들을 정의합니다.
 * 사용자 역할, 상태, 프로필 정보 등이 포함됩니다.
 */

/**
 * 사용자 역할 타입
 * 시스템 내 사용자의 권한 수준을 정의합니다.
 * - CUSTOMER: 반려인
 * - PARTNER: 펫시터
 * - ADMIN: 관리자
 * - SUPER_ADMIN: 최고 관리자
 */
export type UserRole = 'CUSTOMER' | 'PARTNER' | 'ADMIN' | 'SUPER_ADMIN';

/**
 * 사용자 상태 타입
 * 계정의 현재 상태를 나타냅니다.
 * - ACTIVE: 활성 상태
 * - INACTIVE: 비활성 상태
 * - SUSPENDED: 정지 상태
 * - DELETED: 삭제됨
 */
export type UserStatus = 'ACTIVE' | 'INACTIVE' | 'SUSPENDED' | 'DELETED';

/**
 * 사용자 상세 정보 인터페이스
 * 사용자의 전체 정보를 포함합니다.
 */
export interface User {
  /** 사용자 고유 식별자 */
  id: number;
  /** 이메일 주소 (로그인 아이디로 사용) */
  email: string;
  /** 사용자 이름 */
  name: string;
  /** 전화번호 (선택적) */
  phone?: string;
  /** 사용자 역할 */
  role: UserRole;
  /** 계정 상태 */
  status: UserStatus;
  /** 프로필 이미지 URL (선택적) */
  profileImageUrl?: string;
  /** 마지막 로그인 일시 (선택적) */
  lastLoginAt?: string;
  /** 계정 생성 일시 */
  createdAt: string;
  /** 정보 수정 일시 */
  updatedAt: string;
}

/**
 * 간소화된 사용자 정보 인터페이스
 * 목록 표시 등에서 사용되는 최소한의 사용자 정보입니다.
 */
export interface UserSimple {
  /** 사용자 고유 식별자 */
  id: number;
  /** 이메일 주소 */
  email: string;
  /** 사용자 이름 */
  name: string;
  /** 사용자 역할 */
  role: UserRole;
  /** 계정 상태 */
  status: UserStatus;
}

/**
 * 사용자 생성 요청 인터페이스
 * 새 사용자를 생성할 때 필요한 정보입니다.
 */
export interface CreateUserRequest {
  /** 이메일 주소 (필수) */
  email: string;
  /** 비밀번호 (필수) */
  password: string;
  /** 사용자 이름 (필수) */
  name: string;
  /** 전화번호 (선택적) */
  phone?: string;
  /** 사용자 역할 (선택적, 기본값: USER) */
  role?: UserRole;
}

/**
 * 사용자 정보 수정 요청 인터페이스
 * 기존 사용자 정보를 수정할 때 사용됩니다.
 */
export interface UpdateUserRequest {
  /** 수정할 이름 (선택적) */
  name?: string;
  /** 수정할 전화번호 (선택적) */
  phone?: string;
  /** 수정할 프로필 이미지 URL (선택적) */
  profileImageUrl?: string;
}

/**
 * 비밀번호 변경 요청 인터페이스
 * 사용자가 비밀번호를 변경할 때 사용됩니다.
 */
export interface UpdatePasswordRequest {
  /** 현재 비밀번호 */
  currentPassword: string;
  /** 새 비밀번호 */
  newPassword: string;
}
