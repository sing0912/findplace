/**
 * @fileoverview React 애플리케이션의 진입점 파일
 *
 * 이 파일은 React 애플리케이션을 DOM에 마운트하고 초기화하는 역할을 합니다.
 * React.StrictMode를 사용하여 개발 모드에서 잠재적인 문제를 감지합니다.
 */

import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';
import App from './App';
import reportWebVitals from './reportWebVitals';

/**
 * React 애플리케이션을 DOM의 root 요소에 마운트
 * StrictMode로 감싸서 개발 모드에서 추가적인 검사를 수행
 */
const root = ReactDOM.createRoot(
  document.getElementById('root') as HTMLElement
);
root.render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);

// 성능 측정을 위한 웹 바이탈 리포트
// 결과를 콘솔에 로그하거나 분석 엔드포인트로 전송할 수 있습니다.
// 자세한 정보: https://bit.ly/CRA-vitals
reportWebVitals();
