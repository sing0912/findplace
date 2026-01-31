/**
 * @fileoverview API 모듈 진입점
 *
 * 모든 API 관련 모듈을 하나로 통합하여 내보냅니다.
 * 다른 파일에서 API 기능을 임포트할 때 이 파일을 통해 접근할 수 있습니다.
 *
 * @example
 * import { apiClient, authApi, userApi } from './api';
 */

export { default as apiClient } from './client';
export * from './auth';
export * from './user';
export * from './funeralHome';
export * from './pet';
export * from './region';
