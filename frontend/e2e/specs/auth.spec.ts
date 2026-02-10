import { test, expect } from '@playwright/test';
import { mockApi, wrapResponse, removeOverlay } from '../helpers';
import { fixtures } from '../fixtures';

test.describe('로그인 페이지', () => {
  test.beforeEach(async ({ page }) => {
    await removeOverlay(page);
    await page.goto('/login');
  });

  test('앱 이름과 슬로건을 표시한다', async ({ page }) => {
    await expect(page.getByRole('heading', { name: '펫프로' })).toBeVisible();
    await expect(page.getByText('돌봄이 필요한 순간, 펫프로')).toBeVisible();
  });

  test('소셜 로그인 버튼 3개를 표시한다', async ({ page }) => {
    await expect(page.getByText('카카오로 시작하기')).toBeVisible();
    await expect(page.getByText('네이버로 시작하기')).toBeVisible();
    await expect(page.getByText('Google로 시작하기')).toBeVisible();
  });

  test('이메일로 회원가입 링크를 클릭하면 회원가입 페이지로 이동한다', async ({ page }) => {
    await page.getByText('이메일로 회원가입').click();
    await expect(page).toHaveURL('/register');
  });

  test('아이디 찾기 링크를 클릭하면 아이디 찾기 페이지로 이동한다', async ({ page }) => {
    await page.getByText('아이디 찾기', { exact: true }).click();
    await expect(page).toHaveURL('/find-id');
  });

  test('비밀번호 찾기 링크를 클릭하면 비밀번호 재설정 페이지로 이동한다', async ({ page }) => {
    await page.getByText('비밀번호 찾기', { exact: true }).click();
    await expect(page).toHaveURL('/reset-password');
  });
});

test.describe('회원가입 Step1 - 약관동의', () => {
  test.beforeEach(async ({ page }) => {
    await removeOverlay(page);
    await page.goto('/register');
  });

  test('회원가입 헤더와 약관 동의 체크박스를 표시한다', async ({ page }) => {
    await expect(page.getByText('회원가입')).toBeVisible();
    await expect(page.getByText('약관에 동의해 주세요')).toBeVisible();
    await expect(page.getByText('전체 동의')).toBeVisible();
    await expect(page.getByText('이용약관 동의')).toBeVisible();
    await expect(page.getByText('개인정보처리방침 동의')).toBeVisible();
    await expect(page.getByText('마케팅 정보 수신 동의')).toBeVisible();
  });

  test('필수 약관 미동의 시 다음 버튼이 비활성화된다', async ({ page }) => {
    const nextButton = page.getByRole('button', { name: '다음' });
    await expect(nextButton).toBeDisabled();
  });

  test('전체 동의 체크 시 모든 체크박스가 체크된다', async ({ page }) => {
    await page.getByText('전체 동의').click();
    await expect(page.locator('#agree-terms')).toBeChecked();
    await expect(page.locator('#agree-privacy')).toBeChecked();
    await expect(page.locator('#agree-marketing')).toBeChecked();
  });

  test('필수 약관(이용약관, 개인정보) 동의 시 다음 버튼이 활성화된다', async ({ page }) => {
    await page.getByText('이용약관 동의').click();
    await page.getByText('개인정보처리방침 동의').click();
    const nextButton = page.getByRole('button', { name: '다음' });
    await expect(nextButton).toBeEnabled();
  });

  test('다음 버튼 클릭 시 Step2 페이지로 이동한다', async ({ page }) => {
    await page.getByText('이용약관 동의').click();
    await page.getByText('개인정보처리방침 동의').click();
    await page.getByRole('button', { name: '다음' }).click();
    await expect(page).toHaveURL('/register/info');
  });
});

test.describe('회원가입 Step2 - 정보 입력', () => {
  test.beforeEach(async ({ page }) => {
    await removeOverlay(page);
    await page.goto('/register');
    await page.getByText('이용약관 동의').click();
    await page.getByText('개인정보처리방침 동의').click();
    await page.getByRole('button', { name: '다음' }).click();
    await expect(page).toHaveURL('/register/info');
  });

  test('회원가입 폼 필드들을 표시한다', async ({ page }) => {
    await expect(page.getByText('회원가입')).toBeVisible();
    await expect(page.locator('input[name="email"]')).toBeVisible();
    await expect(page.locator('input[name="password"]')).toBeVisible();
    await expect(page.locator('input[name="passwordConfirm"]')).toBeVisible();
    await expect(page.locator('input[name="name"]')).toBeVisible();
    await expect(page.locator('input[name="nickname"]')).toBeVisible();
    await expect(page.locator('input[name="phone"]')).toBeVisible();
  });

  test('입력값이 없으면 가입하기 버튼이 비활성화된다', async ({ page }) => {
    const submitButton = page.getByRole('button', { name: '가입하기' });
    await expect(submitButton).toBeDisabled();
  });

  test('유효한 정보 입력 후 가입에 성공하면 완료 페이지로 이동한다', async ({ page }) => {
    await mockApi(page, [
      { method: 'GET', url: 'auth/check-email*', body: { available: true } },
      { method: 'GET', url: 'auth/check-nickname*', body: { available: true } },
      { method: 'POST', url: 'auth/register', body: wrapResponse(fixtures.auth.registerResult) },
    ]);

    await page.locator('input[name="email"]').fill('newuser@test.com');
    await page.locator('input[name="email"]').blur();
    await page.waitForTimeout(300);
    await page.locator('input[name="password"]').fill('Test1234!');
    await page.locator('input[name="password"]').blur();
    await page.locator('input[name="passwordConfirm"]').fill('Test1234!');
    await page.locator('input[name="passwordConfirm"]').blur();
    await page.locator('input[name="name"]').fill('테스트유저');
    await page.locator('input[name="name"]').blur();
    await page.locator('input[name="nickname"]').fill('테스트닉네임');
    await page.locator('input[name="nickname"]').blur();
    await page.waitForTimeout(300);
    await page.locator('input[name="phone"]').fill('01012345678');
    await page.locator('input[name="phone"]').blur();

    await page.waitForTimeout(500);

    const submitButton = page.getByRole('button', { name: '가입하기' });
    await expect(submitButton).toBeEnabled();
    await submitButton.click();

    await expect(page).toHaveURL('/register/complete');
  });
});

test.describe('아이디 찾기', () => {
  test.beforeEach(async ({ page }) => {
    await removeOverlay(page);
    await page.goto('/find-id');
  });

  test('아이디 찾기 페이지를 표시한다', async ({ page }) => {
    await expect(page.getByText('아이디 찾기', { exact: true }).first()).toBeVisible();
    await expect(page.getByText('회원가입 시 등록한 이름과 휴대폰 번호를 입력해주세요.')).toBeVisible();
    await expect(page.locator('input[name="name"]')).toBeVisible();
    await expect(page.locator('input[name="phone"]')).toBeVisible();
  });

  test('입력값이 없으면 인증번호 받기 버튼이 비활성화된다', async ({ page }) => {
    const submitButton = page.getByRole('button', { name: '인증번호 받기' });
    await expect(submitButton).toBeDisabled();
  });

  test('유효한 정보 입력 후 제출 시 인증번호 화면으로 이동한다', async ({ page }) => {
    await page.route('**/api/v1/auth/find-id/request', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ requestId: 'req-123' }),
      });
    });

    await page.locator('input[name="name"]').fill('테스트반려인');
    await page.locator('input[name="phone"]').fill('01012345678');

    const submitButton = page.getByRole('button', { name: '인증번호 받기' });
    await expect(submitButton).toBeEnabled();
    await submitButton.click();

    await expect(page).toHaveURL('/find-id/verify');
  });
});

test.describe('비밀번호 재설정', () => {
  test.beforeEach(async ({ page }) => {
    await removeOverlay(page);
    await page.goto('/reset-password');
  });

  test('비밀번호 재설정 페이지를 표시한다', async ({ page }) => {
    await expect(page.getByText('비밀번호 재설정', { exact: true }).first()).toBeVisible();
    await expect(page.getByText('회원가입 시 등록한 이메일과 휴대폰 번호를 입력해주세요.')).toBeVisible();
    await expect(page.locator('input[name="email"]')).toBeVisible();
    await expect(page.locator('input[name="phone"]')).toBeVisible();
  });

  test('입력값이 없으면 인증번호 받기 버튼이 비활성화된다', async ({ page }) => {
    const submitButton = page.getByRole('button', { name: '인증번호 받기' });
    await expect(submitButton).toBeDisabled();
  });

  test('유효한 정보 입력 후 제출 시 인증번호 화면으로 이동한다', async ({ page }) => {
    await page.route('**/api/v1/auth/reset-password/request', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ requestId: 'req-456' }),
      });
    });

    await page.locator('input[name="email"]').fill('customer@test.com');
    await page.locator('input[name="phone"]').fill('01012345678');

    const submitButton = page.getByRole('button', { name: '인증번호 받기' });
    await expect(submitButton).toBeEnabled();
    await submitButton.click();

    await expect(page).toHaveURL('/reset-password/verify');
  });
});
