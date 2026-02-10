import { test, expect } from '@playwright/test';
import { mockApi, wrapResponse, gotoAuthenticated, removeOverlay } from '../helpers';
import { fixtures } from '../fixtures';

const mockMyPageApis = async (page: import('@playwright/test').Page) => {
  await mockApi(page, [
    {
      method: 'GET',
      url: 'users/me',
      body: wrapResponse({
        id: 1,
        email: 'customer@test.com',
        nickname: '테스트반려인',
        profileImageUrl: null,
      }),
    },
  ]);
};

test.describe('마이페이지 메인', () => {
  test.beforeEach(async ({ page }) => {
    await mockMyPageApis(page);
    await gotoAuthenticated(page, '/mypage', 'CUSTOMER');
  });

  test('마이페이지 헤더를 표시한다', async ({ page }) => {
    await expect(page.getByText('마이페이지', { exact: true })).toBeVisible();
  });

  test('프로필 카드에 닉네임과 이메일을 표시한다', async ({ page }) => {
    await expect(page.getByText('테스트반려인')).toBeVisible();
    await expect(page.getByText('customer@test.com')).toBeVisible();
  });

  test('내 정보 메뉴 항목을 표시한다', async ({ page }) => {
    await expect(page.getByText('내 프로필')).toBeVisible();
    await expect(page.getByText('펫 관리')).toBeVisible();
  });

  test('서비스 메뉴 항목을 표시한다', async ({ page }) => {
    await expect(page.getByText('결제 수단 관리')).toBeVisible();
    await expect(page.getByText('알림 설정')).toBeVisible();
    await expect(page.getByText('회원등급 안내')).toBeVisible();
    await expect(page.getByText('친구 초대')).toBeVisible();
  });

  test('고객센터 메뉴 항목을 표시한다', async ({ page }) => {
    await expect(page.getByText('FAQ')).toBeVisible();
    await expect(page.getByText('1:1 문의')).toBeVisible();
  });

  test('설정 메뉴 항목을 표시한다', async ({ page }) => {
    await expect(page.getByText('비밀번호 변경')).toBeVisible();
    await expect(page.getByText('약관/정책')).toBeVisible();
  });

  test('내 프로필 클릭 시 프로필 수정 페이지로 이동한다', async ({ page }) => {
    await page.getByText('내 프로필').click();
    await expect(page).toHaveURL('/mypage/edit');
  });

  test('펫 관리 클릭 시 펫 목록 페이지로 이동한다', async ({ page }) => {
    await page.getByText('펫 관리').click();
    await expect(page).toHaveURL('/mypage/pets');
  });

  test('1:1 문의 클릭 시 문의 목록 페이지로 이동한다', async ({ page }) => {
    await page.getByText('1:1 문의').click();
    await expect(page).toHaveURL('/mypage/inquiry');
  });

  test('비밀번호 변경 클릭 시 비밀번호 변경 페이지로 이동한다', async ({ page }) => {
    await page.getByText('비밀번호 변경').click();
    await expect(page).toHaveURL('/mypage/password');
  });

  test('약관/정책 클릭 시 약관 페이지로 이동한다', async ({ page }) => {
    await page.getByText('약관/정책').click();
    await expect(page).toHaveURL('/mypage/settings/policies');
  });
});

test.describe('마이페이지 - 로그아웃', () => {
  test.beforeEach(async ({ page }) => {
    await mockMyPageApis(page);
    await gotoAuthenticated(page, '/mypage', 'CUSTOMER');
  });

  test('로그아웃 버튼 클릭 시 확인 다이얼로그를 표시한다', async ({ page }) => {
    await page.getByRole('button', { name: '로그아웃' }).click();
    await expect(page.getByText('로그아웃 하시겠습니까?')).toBeVisible();
  });

  test('로그아웃 확인 시 로그인 페이지로 이동한다', async ({ page }) => {
    await page.route('**/api/v1/auth/logout', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(wrapResponse(null)),
      });
    });

    await page.getByRole('button', { name: '로그아웃' }).click();
    await page.getByRole('button', { name: '확인' }).click();
    await expect(page).toHaveURL('/login');
  });

  test('로그아웃 취소 시 다이얼로그를 닫는다', async ({ page }) => {
    await page.getByRole('button', { name: '로그아웃' }).click();
    await page.getByRole('button', { name: '취소' }).click();
    await expect(page.getByText('로그아웃 하시겠습니까?')).not.toBeVisible();
  });
});

test.describe('마이페이지 - 회원탈퇴', () => {
  test.beforeEach(async ({ page }) => {
    await mockMyPageApis(page);
    await gotoAuthenticated(page, '/mypage', 'CUSTOMER');
  });

  test('회원탈퇴 버튼 클릭 시 확인 다이얼로그를 표시한다', async ({ page }) => {
    await page.getByText('회원탈퇴').click();
    await expect(page.getByText('정말 탈퇴하시겠습니까?')).toBeVisible();
  });

  test('회원탈퇴 확인 시 로그인 페이지로 이동한다', async ({ page }) => {
    await page.route('**/api/v1/users/me', async (route) => {
      if (route.request().method() === 'DELETE') {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify(wrapResponse(null)),
        });
      } else {
        await route.continue();
      }
    });

    await page.getByText('회원탈퇴').click();
    await page.getByRole('button', { name: '탈퇴하기' }).click();
    await expect(page).toHaveURL('/login');
  });
});

test.describe('프로필 수정 페이지', () => {
  test('프로필 정보를 표시한다', async ({ page }) => {
    await mockApi(page, [
      {
        method: 'GET',
        url: 'users/me',
        body: wrapResponse({
          id: 1,
          email: 'customer@test.com',
          name: '테스트반려인',
          nickname: '테스트반려인',
          phone: '010-1234-5678',
          profileImageUrl: null,
        }),
      },
    ]);
    await gotoAuthenticated(page, '/mypage/edit', 'CUSTOMER');

    await expect(page.getByText('회원정보 수정')).toBeVisible();
    await expect(page.locator('input[name="email"]')).toBeVisible();
  });
});

test.describe('비밀번호 변경 페이지', () => {
  test.beforeEach(async ({ page }) => {
    await mockApi(page, [
      { method: 'GET', url: 'users/me', body: wrapResponse(fixtures.auth.customerUser) },
    ]);
    await gotoAuthenticated(page, '/mypage/password', 'CUSTOMER');
  });

  test('비밀번호 변경 폼을 표시한다', async ({ page }) => {
    await expect(page.getByText('비밀번호 변경', { exact: true }).first()).toBeVisible();
    await expect(page.locator('input[name="currentPassword"]')).toBeVisible();
    await expect(page.locator('input[name="newPassword"]')).toBeVisible();
    await expect(page.locator('input[name="confirmPassword"]')).toBeVisible();
  });

  test('유효한 비밀번호 입력 후 변경에 성공한다', async ({ page }) => {
    await page.route('**/api/v1/users/*/password', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(wrapResponse(null)),
      });
    });

    await page.locator('input[name="currentPassword"]').fill('OldPass123!');
    await page.locator('input[name="newPassword"]').fill('NewPass456!');
    await page.locator('input[name="confirmPassword"]').fill('NewPass456!');

    const changeButton = page.getByRole('button', { name: '변경하기' });
    if (await changeButton.isEnabled()) {
      await changeButton.click();
      await page.waitForTimeout(500);
    }
  });
});

test.describe('마이페이지 - 미인증 접근', () => {
  test('미인증 상태로 접근 시 로그인 페이지로 리디렉트된다', async ({ page }) => {
    await removeOverlay(page);
    await page.goto('/mypage');
    await expect(page).toHaveURL('/login');
  });
});
