/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 *  the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {defineConfig, devices} from '@playwright/test';

export default defineConfig({
  testDir: './playwright-tests',

  fullyParallel: true,
  forbidOnly: Boolean(process.env.CI),
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI
    ? Number(process.env.PLAYWRIGHT_WORKERS ?? 1)
    : undefined,

  expect: {
    timeout: 10_000,
  },

  reporter: [
    ['list'],
    [
      'html',
      {
        outputFolder: 'playwright-report',
        open: 'never',
      },
    ],
  ],

  use: {
    baseURL: process.env.E2E_BASE_URL ?? 'http://127.0.0.1:4200',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
  },

  projects: [
    {
      name: 'chromium-desktop',
      use: {...devices['Desktop Chrome']},
    },
    {
      name: 'firefox-desktop',
      use: {...devices['Desktop Firefox']},
    },
    {
      name: 'webkit-desktop',
      use: {...devices['Desktop Safari']},
    },
    {
      name: 'android-pixel',
      use: {...devices['Pixel 7']},
    },
    {
      name: 'ios-iphone',
      use: {...devices['iPhone 14']},
    },
    {
      name: 'ios-ipad',
      use: {...devices['iPad Pro 11']},
    },
  ],

  webServer: process.env.E2E_BASE_URL
    ? undefined
    : {
      command: 'npm start -- --port 4200',
      url: 'http://127.0.0.1:4200',
      reuseExistingServer: !process.env.CI,
      timeout: 300_000,
      stdout: 'pipe',
      stderr: 'pipe',
    },
});
