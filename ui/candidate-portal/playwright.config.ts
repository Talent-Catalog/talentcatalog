/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
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
import {candidateAuthFile,} from './playwright-tests/support/auth-state';
import {getE2EBaseUrl} from "./playwright-tests/support/e2e-env";

const authenticationSetupPattern = /.*\.auth\.setup\.ts/;

const browserTestIgnorePatterns = [
  authenticationSetupPattern,
];

const baseURL = getE2EBaseUrl();

/**
 * Returns the configured Playwright worker count.
 *
 * Candidate portal E2E tests default to one worker because local development
 * uses one Angular server, one backend and one shared authenticated candidate.
 * Developers and CI can explicitly increase the value after proving that the
 * target environment supports concurrent browser sessions.
 *
 * @returns positive Playwright worker count
 */
function getWorkerCount(): number {
  const rawValue = process.env.PLAYWRIGHT_WORKERS ?? '1';
  const workerCount = Number.parseInt(rawValue, 10);

  if (!Number.isInteger(workerCount) || workerCount < 1) {
    throw new Error(
      'PLAYWRIGHT_WORKERS must be a positive integer.',
    );
  }

  return workerCount;
}

/**
 * Playwright configuration for candidate-portal E2E coverage.
 *
 * The authentication setup project signs in once and saves the dedicated GRN
 * candidate session. Each browser and emulated-device project then starts with
 * that authenticated state.
 */
export default defineConfig({
  testDir: './playwright-tests',

  /*
  * Test files can be parallelized later when the suite has isolated accounts
  * and an environment capable of handling concurrent browser projects.
  */
  fullyParallel: false,

  forbidOnly: Boolean(process.env.CI),

  retries: process.env.CI ? 2 : 0,

  /*
   * Run projects sequentially by default. Override with
   * PLAYWRIGHT_WORKERS only when the environment supports concurrency.
   */
  workers: getWorkerCount(),

  expect: {
    timeout: 15_000,
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
    baseURL,
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',

    navigationTimeout: 30_000,
    actionTimeout: 20_000,
  },

  projects: [
    /*
     * Runs first and creates the candidate authentication state.
     * Dependent projects will not run when authentication fails.
     */
    {
      name: 'auth-setup',
      testMatch: authenticationSetupPattern,
      use: {
        ...devices['Desktop Chrome'],
      },
    },

    {
      name: 'chromium-desktop',
      testIgnore: browserTestIgnorePatterns,
      dependencies: ['auth-setup'],
      use: {
        ...devices['Desktop Chrome'],
        storageState: candidateAuthFile,
      },
    },

    {
      name: 'firefox-desktop',
      testIgnore: browserTestIgnorePatterns,
      dependencies: ['auth-setup'],
      use: {
        ...devices['Desktop Firefox'],
        storageState: candidateAuthFile,
      },
    },

    {
      name: 'webkit-desktop',
      testIgnore: browserTestIgnorePatterns,
      dependencies: ['auth-setup'],
      use: {
        ...devices['Desktop Safari'],
        storageState: candidateAuthFile,
      },
    },

    {
      name: 'android-pixel',
      testIgnore: browserTestIgnorePatterns,
      dependencies: ['auth-setup'],
      use: {
        ...devices['Pixel 7'],
        storageState: candidateAuthFile,
      },
    },

    {
      name: 'ios-iphone',
      testIgnore: browserTestIgnorePatterns,
      dependencies: ['auth-setup'],
      use: {
        ...devices['iPhone 14'],
        storageState: candidateAuthFile,
      },
    },

    {
      name: 'ios-ipad',
      testIgnore: browserTestIgnorePatterns,
      dependencies: ['auth-setup'],
      use: {
        ...devices['iPad Pro 11'],
        storageState: candidateAuthFile,
      },
    },
  ],

  webServer: process.env.E2E_BASE_URL
    ? undefined
    : {
      command: 'npm start -- --port 4200',
      url: baseURL,
      reuseExistingServer: !process.env.CI,
      timeout: 300_000,
      stdout: 'pipe',
      stderr: 'pipe',
    },
});
