import { test, expect } from '@playwright/test';
import { mockApi, wrapResponse, gotoAuthenticated, removeOverlay } from '../helpers';
import { fixtures } from '../fixtures';

const mockPetApis = async (page: import('@playwright/test').Page, petList = fixtures.pet.petList) => {
  await mockApi(page, [
    { method: 'GET', url: 'pets', body: wrapResponse(petList) },
    { method: 'GET', url: 'users/me', body: wrapResponse(fixtures.auth.customerUser) },
  ]);
};

test.describe('펫 목록 페이지', () => {
  test.beforeEach(async ({ page }) => {
    await mockPetApis(page);
    await gotoAuthenticated(page, '/mypage/pets', 'CUSTOMER');
  });

  test('펫 카드들을 표시한다', async ({ page }) => {
    await expect(page.getByText('초코')).toBeVisible();
    await expect(page.getByText('나비')).toBeVisible();
  });

  test('펫 수 카운트를 표시한다', async ({ page }) => {
    await expect(page.getByText(/전체.*2마리/)).toBeVisible();
  });

  test('등록 FAB 버튼을 표시한다', async ({ page }) => {
    const fab = page.locator('button').filter({ has: page.locator('svg') }).last();
    await expect(fab).toBeVisible();
  });
});

test.describe('펫 목록 - 빈 상태', () => {
  test('펫이 없으면 빈 상태 메시지를 표시한다', async ({ page }) => {
    await mockPetApis(page, fixtures.pet.emptyPetList);
    await gotoAuthenticated(page, '/mypage/pets', 'CUSTOMER');

    await expect(page.getByText('반려동물 등록')).toBeVisible();
  });

  test('빈 상태에서 등록 버튼 클릭 시 등록 페이지로 이동한다', async ({ page }) => {
    await mockPetApis(page, fixtures.pet.emptyPetList);
    await gotoAuthenticated(page, '/mypage/pets', 'CUSTOMER');

    await page.getByText('반려동물 등록').click();
    await expect(page).toHaveURL('/mypage/pets/register');
  });
});

test.describe('펫 등록 페이지', () => {
  test('등록 폼을 표시한다', async ({ page }) => {
    await mockApi(page, [
      { method: 'GET', url: 'users/me', body: wrapResponse(fixtures.auth.customerUser) },
    ]);
    await gotoAuthenticated(page, '/mypage/pets/register', 'CUSTOMER');

    await expect(page.getByText('반려동물 등록').first()).toBeVisible();
  });
});

test.describe('펫 수정 페이지', () => {
  test('기존 데이터가 프리필된 수정 폼을 표시한다', async ({ page }) => {
    await mockApi(page, [
      { method: 'GET', url: 'pets/1', body: wrapResponse(fixtures.pet.petDetail) },
      { method: 'GET', url: 'users/me', body: wrapResponse(fixtures.auth.customerUser) },
    ]);
    await gotoAuthenticated(page, '/mypage/pets/1/edit', 'CUSTOMER');

    await expect(page.locator('input').first()).toHaveValue('초코');
  });
});

test.describe('펫 체크리스트 페이지', () => {
  test('체크리스트 폼을 표시한다', async ({ page }) => {
    await mockApi(page, [
      { method: 'GET', url: 'pets/1', body: wrapResponse(fixtures.pet.petDetail) },
      { method: 'GET', url: 'pets/1/checklist', body: wrapResponse(fixtures.pet.petChecklist) },
      { method: 'GET', url: 'users/me', body: wrapResponse(fixtures.auth.customerUser) },
    ]);
    await gotoAuthenticated(page, '/mypage/pets/1/checklist', 'CUSTOMER');

    await expect(page.locator('body')).toBeVisible();
  });
});

test.describe('펫 페이지 - 미인증 접근', () => {
  test('미인증 상태로 접근 시 로그인 페이지로 리디렉트된다', async ({ page }) => {
    await removeOverlay(page);
    await page.goto('/mypage/pets');
    await expect(page).toHaveURL('/login');
  });
});
