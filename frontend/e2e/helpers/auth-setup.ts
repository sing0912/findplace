import { Page } from '@playwright/test';
import { fixtures } from '../fixtures';

type Role = 'CUSTOMER' | 'PARTNER' | 'ADMIN' | 'SUPER_ADMIN';

function getUserForRole(role: Role) {
  switch (role) {
    case 'CUSTOMER':
      return fixtures.auth.customerUser;
    case 'PARTNER':
      return fixtures.auth.partnerUser;
    case 'ADMIN':
    case 'SUPER_ADMIN':
      return fixtures.auth.adminUser;
  }
}

/**
 * Remove webpack dev server overlay iframe that intercepts pointer events
 */
export async function removeOverlay(page: Page) {
  await page.addInitScript(() => {
    const observer = new MutationObserver(() => {
      const iframe = document.getElementById('webpack-dev-server-client-overlay');
      if (iframe) iframe.remove();
    });
    observer.observe(document.documentElement, { childList: true, subtree: true });
  });
}

export async function setupAuth(page: Page, role: Role = 'CUSTOMER') {
  const user = getUserForRole(role);
  const userWithRole = { ...user, role };

  await page.addInitScript(
    ({ user, accessToken, refreshToken }) => {
      localStorage.setItem('accessToken', accessToken);
      localStorage.setItem('refreshToken', refreshToken);
      localStorage.setItem(
        'auth-storage',
        JSON.stringify({
          state: { user, isAuthenticated: true },
          version: 0,
        }),
      );
    },
    {
      user: userWithRole,
      accessToken: fixtures.auth.tokenResponse.accessToken,
      refreshToken: fixtures.auth.tokenResponse.refreshToken,
    },
  );
}

/**
 * Setup admin UI store with sidebar open
 */
export async function setupAdminUI(page: Page) {
  await page.addInitScript(() => {
    localStorage.setItem(
      'ui-storage',
      JSON.stringify({
        state: { sidebarOpen: true, darkMode: false, notifications: [], globalLoading: false },
        version: 0,
      }),
    );
  });
}

export async function gotoAuthenticated(page: Page, url: string, role: Role = 'CUSTOMER') {
  await removeOverlay(page);
  await setupAuth(page, role);
  if (role === 'ADMIN' || role === 'SUPER_ADMIN') {
    await setupAdminUI(page);
  }
  await page.goto(url);
}

export async function clearAuth(page: Page) {
  await page.evaluate(() => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('auth-storage');
  });
}
