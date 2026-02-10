import { test, expect } from '@playwright/test';
import { mockApi, mockApiError, wrapResponse, gotoAuthenticated, removeOverlay } from '../helpers';
import { fixtures } from '../fixtures';

test.describe('반려인(CUSTOMER) 홈', () => {
  test.beforeEach(async ({ page }) => {
    await mockApi(page, [
      { method: 'GET', url: 'home/customer*', body: wrapResponse(fixtures.home.customerHome) },
      { method: 'GET', url: 'users/me', body: wrapResponse(fixtures.auth.customerUser) },
    ]);
    await gotoAuthenticated(page, '/', 'CUSTOMER');
  });

  test('추천 시터 섹션을 표시한다', async ({ page }) => {
    await expect(page.getByText('추천 시터')).toBeVisible();
    await expect(page.getByText('김시터')).toBeVisible();
    await expect(page.getByText('이시터')).toBeVisible();
  });

  test('이벤트 배너를 표시한다', async ({ page }) => {
    await expect(page.getByText('첫 예약 30% 할인')).toBeVisible();
  });

  test('커뮤니티 피드를 표시한다', async ({ page }) => {
    await expect(page.getByText('커뮤니티')).toBeVisible();
    await expect(page.getByText('강아지 산책 팁 공유')).toBeVisible();
  });
});

test.describe('반려인(CUSTOMER) 홈 - 빈 상태', () => {
  test('데이터가 없을 때 빈 상태를 처리한다', async ({ page }) => {
    await mockApi(page, [
      { method: 'GET', url: 'home/customer*', body: wrapResponse(fixtures.home.emptyCustomerHome) },
      { method: 'GET', url: 'users/me', body: wrapResponse(fixtures.auth.customerUser) },
    ]);
    await gotoAuthenticated(page, '/', 'CUSTOMER');

    await expect(page.getByText('주변에 추천 시터가 없습니다')).toBeVisible();
  });
});

test.describe('반려인(CUSTOMER) 홈 - 에러 처리', () => {
  test('API 실패 시 에러 메시지와 재시도 버튼을 표시한다', async ({ page }) => {
    await mockApiError(page, 'GET', 'home/customer*', 500, 'INTERNAL_ERROR', '서버 오류');
    await mockApi(page, [
      { method: 'GET', url: 'users/me', body: wrapResponse(fixtures.auth.customerUser) },
    ]);
    await gotoAuthenticated(page, '/', 'CUSTOMER');

    await expect(page.getByText('홈 화면을 불러올 수 없습니다')).toBeVisible();
    await expect(page.getByRole('button', { name: '재시도' })).toBeVisible();
  });

  test('재시도 버튼 클릭 시 데이터를 다시 요청한다', async ({ page }) => {
    let requestCount = 0;
    await page.route('**/api/v1/home/customer*', async (route) => {
      requestCount++;
      if (requestCount <= 1) {
        await route.fulfill({
          status: 500,
          contentType: 'application/json',
          body: JSON.stringify({
            success: false,
            error: { code: 'INTERNAL_ERROR', message: '서버 오류' },
            timestamp: new Date().toISOString(),
          }),
        });
      } else {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify(wrapResponse(fixtures.home.customerHome)),
        });
      }
    });
    await mockApi(page, [
      { method: 'GET', url: 'users/me', body: wrapResponse(fixtures.auth.customerUser) },
    ]);
    await gotoAuthenticated(page, '/', 'CUSTOMER');

    await expect(page.getByText('홈 화면을 불러올 수 없습니다')).toBeVisible();
    await page.getByRole('button', { name: '재시도' }).click();

    await expect(page.getByText('김시터')).toBeVisible();
  });
});

test.describe('펫시터(PARTNER) 홈', () => {
  test.beforeEach(async ({ page }) => {
    await mockApi(page, [
      { method: 'GET', url: 'home/partner', body: wrapResponse(fixtures.home.partnerHome) },
      { method: 'GET', url: 'users/me', body: wrapResponse(fixtures.auth.partnerUser) },
    ]);
    await gotoAuthenticated(page, '/', 'PARTNER');
  });

  test('오늘 일정 카드를 표시한다', async ({ page }) => {
    await expect(page.getByText(/오늘의 일정/)).toBeVisible();
    await expect(page.getByText(/초코/)).toBeVisible();
    await expect(page.getByText(/나비/)).toBeVisible();
  });

  test('새 요청 배지를 표시한다', async ({ page }) => {
    await expect(page.getByText('5')).toBeVisible();
  });

  test('수익 요약 카드를 표시한다', async ({ page }) => {
    await expect(page.getByText('이번 달 수익')).toBeVisible();
    await expect(page.getByText(/1,250,000/)).toBeVisible();
  });

  test('공지사항을 표시한다', async ({ page }) => {
    await expect(page.getByText('2월 정산 안내')).toBeVisible();
  });
});

test.describe('펫시터(PARTNER) 홈 - 에러 처리', () => {
  test('API 실패 시 에러 메시지와 재시도 버튼을 표시한다', async ({ page }) => {
    await mockApiError(page, 'GET', 'home/partner', 500, 'INTERNAL_ERROR', '서버 오류');
    await mockApi(page, [
      { method: 'GET', url: 'users/me', body: wrapResponse(fixtures.auth.partnerUser) },
    ]);
    await gotoAuthenticated(page, '/', 'PARTNER');

    await expect(page.getByText('홈 화면을 불러올 수 없습니다')).toBeVisible();
    await expect(page.getByRole('button', { name: '재시도' })).toBeVisible();
  });
});

test.describe('펫시터(PARTNER) 홈 - 빈 상태', () => {
  test('데이터가 없을 때 빈 상태를 처리한다', async ({ page }) => {
    await mockApi(page, [
      { method: 'GET', url: 'home/partner', body: wrapResponse(fixtures.home.emptyPartnerHome) },
      { method: 'GET', url: 'users/me', body: wrapResponse(fixtures.auth.partnerUser) },
    ]);
    await gotoAuthenticated(page, '/', 'PARTNER');

    await expect(page.getByText('오늘 예정된 일정이 없습니다')).toBeVisible();
  });
});
