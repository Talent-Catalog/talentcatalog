import {defineConfig, devices} from '@playwright/test';

/**
 * The first performance target is the TBB Admin Portal on staging.
 * Keep the trailing slash so future relative routes like `search` resolve to
 * /admin-portal/search rather than /search.
 */
function getBaseURL(): string {
  const configured = process.env.PLAYWRIGHT_PERF_BASE_URL?.trim();
  const url = new URL(configured || 'https://tctalent-test.org/admin-portal/');

  if (url.protocol !== 'https:' && url.protocol !== 'http:') {
    throw new Error('PLAYWRIGHT_PERF_BASE_URL must use http:// or https://');
  }

  if (url.username || url.password || url.search || url.hash) {
    throw new Error('PLAYWRIGHT_PERF_BASE_URL must not contain credentials, query, or fragment');
  }

  if (!url.pathname.endsWith('/')) {
    url.pathname += '/';
  }

  return url.toString();
}

export default defineConfig({
  testDir: './tests',
  fullyParallel: false,
  forbidOnly: Boolean(process.env.CI),
  retries: 0,
  workers: 1,
  timeout: 60_000,
  expect: {timeout: 10_000},
  reporter: [
    ['list'],
    ['html', {outputFolder: 'playwright-report', open: 'never'}],
  ],
  outputDir: 'test-results',
  use: {
    baseURL: getBaseURL(),
    actionTimeout: 20_000,
    navigationTimeout: 30_000,
    trace: 'off',
    screenshot: 'only-on-failure',
    video: 'off',
  },
  projects: [
    {
      name: 'chromium',
      use: {...devices['Desktop Chrome']},
    },
  ],
});
