import { Page } from '@playwright/test';

interface RouteConfig {
  method: string;
  url: string;
  body: unknown;
  status?: number;
}

export function wrapResponse(data: unknown) {
  return {
    success: true,
    data,
    timestamp: new Date().toISOString(),
  };
}

export function wrapPageResponse(content: unknown[], page = 0, totalElements?: number) {
  const items = Array.isArray(content) ? content : [];
  const total = totalElements ?? items.length;
  return wrapResponse({
    content: items,
    page: {
      number: page,
      size: 10,
      totalElements: total,
      totalPages: Math.ceil(total / 10),
      first: page === 0,
      last: page >= Math.ceil(total / 10) - 1,
    },
  });
}

export async function mockApi(page: Page, routes: RouteConfig[]) {
  for (const route of routes) {
    await page.route(`**/api/v1/${route.url}`, async (r) => {
      if (r.request().method() === route.method) {
        await r.fulfill({
          status: route.status ?? 200,
          contentType: 'application/json',
          body: JSON.stringify(route.body),
        });
      } else {
        await r.continue();
      }
    });
  }
}

export async function mockApiError(
  page: Page,
  method: string,
  url: string,
  status: number,
  code: string,
  message: string,
) {
  await page.route(`**/api/v1/${url}`, async (r) => {
    if (r.request().method() === method) {
      await r.fulfill({
        status,
        contentType: 'application/json',
        body: JSON.stringify({
          success: false,
          error: { code, message },
          timestamp: new Date().toISOString(),
        }),
      });
    } else {
      await r.continue();
    }
  });
}
