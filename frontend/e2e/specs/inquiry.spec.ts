import { test, expect } from '@playwright/test';
import { mockApi, wrapResponse, wrapPageResponse, gotoAuthenticated, removeOverlay } from '../helpers';
import { fixtures } from '../fixtures';

const mockInquiryApis = async (
  page: import('@playwright/test').Page,
  list = fixtures.inquiry.inquiryList,
) => {
  await mockApi(page, [
    { method: 'GET', url: 'inquiries*', body: wrapPageResponse(list) },
    { method: 'GET', url: 'users/me', body: wrapResponse(fixtures.auth.customerUser) },
  ]);
};

test.describe('문의 목록 페이지', () => {
  test.beforeEach(async ({ page }) => {
    await mockInquiryApis(page);
    await gotoAuthenticated(page, '/mypage/inquiry', 'CUSTOMER');
  });

  test('문의 게시판 헤더와 문의하기 버튼을 표시한다', async ({ page }) => {
    await expect(page.getByText('문의 게시판')).toBeVisible();
    await expect(page.getByText('문의하기')).toBeVisible();
  });

  test('문의 카드 목록을 표시한다', async ({ page }) => {
    await expect(page.getByText('예약 변경 문의')).toBeVisible();
    await expect(page.getByText('결제 오류 문의')).toBeVisible();
    await expect(page.getByText('시터 자격 관련 문의')).toBeVisible();
  });

  test('답변 상태 배지를 표시한다', async ({ page }) => {
    await expect(page.getByText('답변완료')).toBeVisible();
    await expect(page.getByText('답변대기').first()).toBeVisible();
  });

  test('문의하기 버튼 클릭 시 작성 페이지로 이동한다', async ({ page }) => {
    await page.getByText('문의하기').click();
    await expect(page).toHaveURL('/mypage/inquiry/write');
  });

  test('문의 카드 클릭 시 상세 페이지로 이동한다', async ({ page }) => {
    await mockApi(page, [
      { method: 'GET', url: 'inquiries/1', body: wrapResponse(fixtures.inquiry.inquiryDetail) },
    ]);

    await page.getByText('예약 변경 문의').click();
    await expect(page).toHaveURL('/mypage/inquiry/1');
  });
});

test.describe('문의 목록 - 빈 상태', () => {
  test('문의가 없으면 빈 상태 메시지를 표시한다', async ({ page }) => {
    await mockInquiryApis(page, fixtures.inquiry.emptyInquiryList);
    await gotoAuthenticated(page, '/mypage/inquiry', 'CUSTOMER');

    await expect(page.getByText('등록된 문의가 없습니다.')).toBeVisible();
    await expect(page.getByText('첫 번째 문의 작성하기')).toBeVisible();
  });

  test('빈 상태에서 작성 버튼 클릭 시 작성 페이지로 이동한다', async ({ page }) => {
    await mockInquiryApis(page, fixtures.inquiry.emptyInquiryList);
    await gotoAuthenticated(page, '/mypage/inquiry', 'CUSTOMER');

    await page.getByText('첫 번째 문의 작성하기').click();
    await expect(page).toHaveURL('/mypage/inquiry/write');
  });
});

test.describe('문의 작성 페이지', () => {
  test.beforeEach(async ({ page }) => {
    await mockApi(page, [
      { method: 'GET', url: 'users/me', body: wrapResponse(fixtures.auth.customerUser) },
    ]);
    await gotoAuthenticated(page, '/mypage/inquiry/write', 'CUSTOMER');
  });

  test('문의 작성 폼을 표시한다', async ({ page }) => {
    await expect(page.getByText('문의하기')).toBeVisible();
    await expect(page.locator('#inquiry-title')).toBeVisible();
    await expect(page.locator('#inquiry-content')).toBeVisible();
  });

  test('입력값이 없으면 등록 버튼이 비활성화된다', async ({ page }) => {
    const submitButton = page.getByRole('button', { name: '등록' });
    await expect(submitButton).toBeDisabled();
  });

  test('제목과 내용 입력 후 등록에 성공한다', async ({ page }) => {
    await mockApi(page, [
      { method: 'POST', url: 'inquiries', body: wrapResponse(fixtures.inquiry.createdInquiry) },
      { method: 'GET', url: 'inquiries/4', body: wrapResponse(fixtures.inquiry.createdInquiry) },
    ]);

    await page.locator('#inquiry-title').fill('새 문의 제목');
    await page.locator('#inquiry-content').fill('새 문의 내용입니다.');

    const submitButton = page.getByRole('button', { name: '등록' });
    await expect(submitButton).toBeEnabled();
    await submitButton.click();

    await expect(page).toHaveURL('/mypage/inquiry/4');
  });
});

test.describe('문의 상세 페이지 - 답변 있는 문의', () => {
  test.beforeEach(async ({ page }) => {
    await mockApi(page, [
      { method: 'GET', url: 'inquiries/1', body: wrapResponse(fixtures.inquiry.inquiryDetail) },
      { method: 'GET', url: 'users/me', body: wrapResponse(fixtures.auth.customerUser) },
    ]);
    await gotoAuthenticated(page, '/mypage/inquiry/1', 'CUSTOMER');
  });

  test('문의 제목과 내용을 표시한다', async ({ page }) => {
    await expect(page.getByText('문의 상세')).toBeVisible();
    await expect(page.getByText('예약 변경 문의')).toBeVisible();
    await expect(page.getByText(/예약 날짜를 변경하고 싶습니다/)).toBeVisible();
  });

  test('답변완료 상태 배지를 표시한다', async ({ page }) => {
    await expect(page.getByText('답변완료')).toBeVisible();
  });

  test('답변 내용을 표시한다', async ({ page }) => {
    await expect(page.getByText('답변', { exact: true })).toBeVisible();
    await expect(page.getByText(/예약 변경은 마이페이지에서 가능합니다/)).toBeVisible();
  });

  test('답변 완료된 문의는 수정/삭제 버튼이 없다', async ({ page }) => {
    await expect(page.getByRole('button', { name: '수정' })).not.toBeVisible();
    await expect(page.getByRole('button', { name: '삭제' })).not.toBeVisible();
  });
});

test.describe('문의 상세 페이지 - 대기중 문의', () => {
  test.beforeEach(async ({ page }) => {
    await mockApi(page, [
      { method: 'GET', url: 'inquiries/2', body: wrapResponse(fixtures.inquiry.inquiryDetailWaiting) },
      { method: 'GET', url: 'users/me', body: wrapResponse(fixtures.auth.customerUser) },
    ]);
    await gotoAuthenticated(page, '/mypage/inquiry/2', 'CUSTOMER');
  });

  test('답변대기 상태 배지를 표시한다', async ({ page }) => {
    await expect(page.getByText('답변대기')).toBeVisible();
  });

  test('수정/삭제 버튼을 표시한다', async ({ page }) => {
    await expect(page.getByRole('button', { name: '수정' })).toBeVisible();
    await expect(page.getByRole('button', { name: '삭제' })).toBeVisible();
  });

  test('삭제 버튼 클릭 시 확인 다이얼로그를 표시한다', async ({ page }) => {
    await page.getByRole('button', { name: '삭제' }).click();
    await expect(page.getByText('이 문의를 삭제하시겠습니까?')).toBeVisible();
  });
});
