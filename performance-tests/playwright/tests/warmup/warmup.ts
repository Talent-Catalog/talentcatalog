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

import {expect, type Page} from '@playwright/test';

const WAIT_MS = 45_000;

/**
 * Read credentials for the staging performance warm-up.
 * Credentials must be provided through environment variables.
 */
export function readWarmUpCredentials(
    env: NodeJS.ProcessEnv = process.env
) {
  const username = env.PLAYWRIGHT_PERF_USERNAME?.trim();
  const password = env.PLAYWRIGHT_PERF_PASSWORD;
  const totpToken = env.PLAYWRIGHT_PERF_TOTP?.trim();

  if (!username || !password) {
    throw new Error(
        '[perf-warmup] Missing PLAYWRIGHT_PERF_USERNAME or ' +
        'PLAYWRIGHT_PERF_PASSWORD for the staging account.'
    );
  }

  return {username, password, totpToken};
}



/**
 * Fill sensitive login fields without putting the secret
 * directly in a Playwright fill() action.
 *
 * This helps avoid exposing passwords and MFA codes
 * in the HTML report's action descriptions.
 */
export async function fillSensitiveInput(
    page: Page,
    controlName: 'password' | 'totpToken',
    secret: string
): Promise<void> {
  const input = page.locator(
      `tc-input[formcontrolname="${controlName}"] input`
  );

  await expect(input).toBeVisible({timeout: WAIT_MS});

  await input.evaluate((element, value) => {
    const field = element as HTMLInputElement;

    const setter = Object.getOwnPropertyDescriptor(
        HTMLInputElement.prototype,
        'value'
    )?.set;

    if (!setter) {
      throw new Error('Native input setter unavailable');
    }

    setter.call(field, value);

    // Notify Angular's form control that the value changed.
    field.dispatchEvent(
        new Event('input', {bubbles: true})
    );
    field.dispatchEvent(
        new Event('change', {bubbles: true})
    );
  }, secret);
}

/**
 * Execute a warm-up step and provide a clear error if it fails.
 */
export async function warmUpStep(
    name: string,
    action: () => Promise<void>
): Promise<void> {
  try {
    await action();
  } catch (error) {
    const reason =
        error instanceof Error ? error.message : String(error);

    throw new Error(
        `[perf-warmup] ${name} failed: ${reason}`
    );
  }
}

/**
 * Navigate to an Admin Portal page and verify the HTTP response.
 */
async function openPage(
    page: Page,
    route: string
): Promise<void> {
  const response = await page.goto(route, {
    waitUntil: 'domcontentloaded',
    timeout: WAIT_MS,
  });

  if (!response?.ok()) {
    throw new Error(
        `Opening ${route} returned HTTP ` +
        `${response?.status() ?? 'no response'}`
    );
  }
}

/**
 * Warm up the TBB staging Admin Portal before performance measurements.
 *
 * Flow:
 * 1. Login
 * 2. Candidate Search
 * 3. Jobs
 *
 * Warm-up results are not included in performance measurements.
 * Any failed step stops the warm-up with a clear error.
 */
export async function warmUpAdminPortal(
    page: Page
): Promise<void> {
  const credentials = readWarmUpCredentials();

  // 1. Login to the staging Admin Portal.
  await warmUpStep('Login', async () => {
    await openPage(page, 'login');

    await page
    .locator('tc-input[formcontrolname="username"] input')
    .fill(credentials.username);

    await fillSensitiveInput(
        page,
        'password',
        credentials.password
    );

    if (credentials.totpToken) {
      await fillSensitiveInput(
          page,
          'totpToken',
          credentials.totpToken
      );
    }

    await page.getByRole('button', {
      name: 'Login',
      exact: true,
    }).click();

    await expect(page).toHaveURL(
        /\/admin-portal\/jobs(?:\/|[?#]|$)/,
        {timeout: WAIT_MS}
    );
  });

  // 2. Warm up Candidate Search using the default search.
  // Wait until the actual results have loaded.
  await warmUpStep('Candidate Search', async () => {
    await openPage(page, 'search');

    await expect(
        page.locator('app-define-search')
    ).toBeVisible({timeout: WAIT_MS});

    await expect(
        page
        .locator('app-show-candidates .results-area')
        .getByText(/^\d+\s+candidates returned$/)
    ).toBeVisible({timeout: WAIT_MS});
  });

  // 3. Warm up Jobs, including the initial backend search.
  await warmUpStep('Jobs', async () => {

    // Start listening before navigating to Jobs.
    const jobsResponsePromise = page.waitForResponse(
        response =>
            response.request().method() === 'POST' &&
            new URL(response.url()).pathname.endsWith(
                '/job/search-paged'
            ),
        {timeout: WAIT_MS}
    );

    // Attach the rejection handler immediately.
    // Awaiting the original promise still reports failures.
    void jobsResponsePromise.catch(() => {});

    // Navigate to the Jobs page.
    await openPage(page, 'jobs');

    // Wait for the backend response.
    const response = await jobsResponsePromise;

    if (!response.ok()) {
      throw new Error(
          `Jobs search returned HTTP ${response.status()}`
      );
    }

    // Wait for the response body to finish.
    await response.finished();

    // Confirm the Jobs table is visible.
    await expect(
        page.locator('app-jobs tc-table table').first()
    ).toBeVisible({timeout: WAIT_MS});
  });

}
