/**
 * @fileoverview API 통신 관련 공통 타입 정의
 *
 * 이 파일은 백엔드 API와의 통신에서 사용되는 공통 타입들을 정의합니다.
 * 모든 API 응답 및 페이지네이션 관련 타입이 포함되어 있습니다.
 */

/**
 * API 응답의 기본 구조
 * 모든 API 응답은 이 형식을 따릅니다.
 * @template T - 응답 데이터의 타입
 */
export interface ApiResponse<T> {
  /** 요청 성공 여부 */
  success: boolean;
  /** 응답 메시지 (선택적) */
  message?: string;
  /** 응답 데이터 (선택적) */
  data?: T;
  /** 에러 상세 정보 (선택적) */
  error?: ErrorDetail;
  /** 응답 타임스탬프 */
  timestamp: string;
}

/**
 * 에러 상세 정보
 * API 요청 실패 시 반환되는 에러 정보를 담습니다.
 */
export interface ErrorDetail {
  /** 에러 코드 */
  code: string;
  /** 에러 메시지 */
  message: string;
  /** 추가 에러 상세 정보 (필드별 에러 등) */
  details?: Record<string, string>;
}

/**
 * 페이지네이션된 응답 구조
 * 목록 조회 API에서 사용됩니다.
 * @template T - 목록 항목의 타입
 */
export interface PageResponse<T> {
  /** 현재 페이지의 데이터 목록 */
  content: T[];
  /** 페이지 정보 */
  page: PageInfo;
}

/**
 * 페이지 정보
 * 현재 페이지 상태 및 전체 데이터 정보를 담습니다.
 */
export interface PageInfo {
  /** 현재 페이지 번호 (0부터 시작) */
  number: number;
  /** 페이지 당 항목 수 */
  size: number;
  /** 전체 항목 수 */
  totalElements: number;
  /** 전체 페이지 수 */
  totalPages: number;
  /** 첫 번째 페이지 여부 */
  first: boolean;
  /** 마지막 페이지 여부 */
  last: boolean;
}

/**
 * 페이지네이션 요청 파라미터
 * 목록 조회 시 페이지네이션 옵션을 지정합니다.
 */
export interface PageRequest {
  /** 요청할 페이지 번호 (0부터 시작) */
  page?: number;
  /** 페이지 당 항목 수 */
  size?: number;
  /** 정렬 기준 (예: "createdAt,desc") */
  sort?: string;
}
