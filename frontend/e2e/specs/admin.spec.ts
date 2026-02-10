import { test, expect } from '@playwright/test';
import { mockApi, wrapResponse, gotoAuthenticated, setupAuth, removeOverlay } from '../helpers';
import { fixtures } from '../fixtures';

test.describe('관리자 로그인', () => {
  test.beforeEach(async ({ page }) => {
    await removeOverlay(page);
    await page.goto('/admin/login');
  });

  test('관리자 로그인 폼을 표시한다', async ({ page }) => {
    await expect(page.getByText('PetPro 관리자')).toBeVisible();
    await expect(page.getByLabel('이메일')).toBeVisible();
    await expect(page.getByLabel('비밀번호')).toBeVisible();
    await expect(page.getByRole('button', { name: '로그인' })).toBeVisible();
  });

  test('이메일/비밀번호 입력 후 로그인에 성공하면 대시보드로 이동한다', async ({ page }) => {
    // useAdminAuth: authApi.login → userApi.getMe() 순서로 호출
    await mockApi(page, [
      {
        method: 'POST',
        url: 'auth/login',
        body: wrapResponse(fixtures.admin.adminTokenResponse),
      },
      {
        method: 'GET',
        url: 'users/me',
        body: wrapResponse(fixtures.auth.adminUser),
      },
    ]);

    await page.getByLabel('이메일').fill('admin@petpro.com');
    await page.getByLabel('비밀번호').fill('Admin1234!');
    await page.getByRole('button', { name: '로그인' }).click();

    await expect(page).toHaveURL('/admin/dashboard');
  });

  test('로그인 실패 시 로그인 페이지에 남아있다', async ({ page }) => {
    await page.route('**/api/v1/auth/login', async (route) => {
      await route.fulfill({
        status: 401,
        contentType: 'application/json',
        body: JSON.stringify({
          success: false,
          error: { code: 'INVALID_CREDENTIALS', message: '이메일 또는 비밀번호가 올바르지 않습니다.' },
          timestamp: new Date().toISOString(),
        }),
      });
    });

    await page.getByLabel('이메일').fill('wrong@test.com');
    await page.getByLabel('비밀번호').fill('wrongpassword');
    await page.getByRole('button', { name: '로그인' }).click();

    await page.waitForTimeout(500);
    await expect(page).toHaveURL('/admin/login');
  });
});

test.describe('관리자 보호 라우트', () => {
  test('미인증 상태로 대시보드 접근 시 로그인 페이지로 리디렉트된다', async ({ page }) => {
    await removeOverlay(page);
    await page.goto('/admin/dashboard');
    await expect(page).toHaveURL('/admin/login');
  });

  test('미인증 상태로 회원관리 접근 시 로그인 페이지로 리디렉트된다', async ({ page }) => {
    await removeOverlay(page);
    await page.goto('/admin/members/users');
    await expect(page).toHaveURL('/admin/login');
  });

  test('미인증 상태로 설정 접근 시 로그인 페이지로 리디렉트된다', async ({ page }) => {
    await removeOverlay(page);
    await page.goto('/admin/settings');
    await expect(page).toHaveURL('/admin/login');
  });
});

test.describe('관리자 역할 검증', () => {
  test('CUSTOMER 역할로 관리자 페이지 접근 시 로그인 페이지로 리디렉트된다', async ({ page }) => {
    await removeOverlay(page);
    await setupAuth(page, 'CUSTOMER');
    await page.goto('/admin/dashboard');
    await expect(page).toHaveURL('/admin/login');
  });

  test('PARTNER 역할로 관리자 페이지 접근 시 로그인 페이지로 리디렉트된다', async ({ page }) => {
    await removeOverlay(page);
    await setupAuth(page, 'PARTNER');
    await page.goto('/admin/dashboard');
    await expect(page).toHaveURL('/admin/login');
  });

  test('ADMIN 역할로 관리자 페이지 접근 시 정상 표시된다', async ({ page }) => {
    await mockApi(page, [
      {
        method: 'GET',
        url: 'users/me',
        body: wrapResponse(fixtures.auth.adminUser),
      },
    ]);
    await gotoAuthenticated(page, '/admin/dashboard', 'ADMIN');

    await expect(page.locator('body')).toBeVisible();
    await expect(page).toHaveURL('/admin/dashboard');
  });
});

test.describe('관리자 사이드바', () => {
  test.beforeEach(async ({ page, browserName }, testInfo) => {
    // Sidebar is persistent drawer - skip on mobile where it may not be visible
    test.skip(testInfo.project.name === 'mobile-chrome', 'Sidebar tests only run on desktop');


    await mockApi(page, [
      {
        method: 'GET',
        url: 'users/me',
        body: wrapResponse(fixtures.auth.adminUser),
      },
    ]);
    await gotoAuthenticated(page, '/admin/dashboard', 'ADMIN');
  });

  test('8개 메인 메뉴를 표시한다', async ({ page }) => {
    await expect(page.getByText('대시보드')).toBeVisible();
    await expect(page.getByText('회원 관리')).toBeVisible();
    await expect(page.getByText('예약 관리')).toBeVisible();
    await expect(page.getByText('정산 관리')).toBeVisible();
    await expect(page.getByText('고객센터')).toBeVisible();
    await expect(page.getByText('콘텐츠 관리')).toBeVisible();
    await expect(page.getByText('통계')).toBeVisible();
    await expect(page.getByText('설정')).toBeVisible();
  });

  test('회원 관리 메뉴 클릭 시 하위 메뉴를 표시한다', async ({ page }) => {
    await page.getByText('회원 관리').click();
    await expect(page.getByText('반려인 관리')).toBeVisible();
    await expect(page.getByText('펫시터 관리')).toBeVisible();
    await expect(page.getByText('시터 심사')).toBeVisible();
  });

  test('고객센터 메뉴 클릭 시 하위 메뉴를 표시한다', async ({ page }) => {
    await page.getByText('고객센터').click();
    await expect(page.getByText('1:1 문의')).toBeVisible();
    await expect(page.getByText('FAQ 관리')).toBeVisible();
  });

  test('콘텐츠 관리 메뉴 클릭 시 하위 메뉴를 표시한다', async ({ page }) => {
    await page.getByText('콘텐츠 관리').click();
    await expect(page.getByText('공지사항')).toBeVisible();
    await expect(page.getByText('이벤트')).toBeVisible();
    await expect(page.getByText('커뮤니티')).toBeVisible();
    await expect(page.getByText('캠페인')).toBeVisible();
  });

  test('설정 메뉴 클릭 시 하위 메뉴를 표시한다', async ({ page }) => {
    await page.getByText('설정').click();
    await expect(page.getByText('알림 설정')).toBeVisible();
    await expect(page.getByText('약관 관리')).toBeVisible();
    await expect(page.getByText('계정 관리')).toBeVisible();
    await expect(page.getByText('감사 로그')).toBeVisible();
    await expect(page.getByText('앱 버전')).toBeVisible();
  });

  test('하위 메뉴 클릭 시 해당 페이지로 이동한다', async ({ page }) => {
    await page.getByText('회원 관리').click();
    await page.getByText('반려인 관리').click();
    await expect(page).toHaveURL('/admin/members/users');
  });
});
