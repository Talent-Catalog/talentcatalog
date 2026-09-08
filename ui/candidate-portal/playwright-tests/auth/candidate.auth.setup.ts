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

import * as fs from 'node:fs';
import * as path from 'node:path';
import {expect, test as setup} from '@playwright/test';

import {candidateAuthFile} from '../support/auth-state';
import {getE2EEnvironment} from "../support/e2e-env";


/**
 * Authenticates a dedicated GRN candidate and saves the resulting browser
 * state for the dependent Playwright projects.
 *
 * The candidate portal stores authentication information in local storage.
 * Saving the browser context therefore preserves the JWT, logged-in user,
 * candidate instance type and other session values needed by protected routes.
 */
setup('authenticate dedicated GRN candidate', async ({page}) => {
  const {
    candidateUsername,
    candidatePassword,
  } = getE2EEnvironment();

  fs.mkdirSync(path.dirname(candidateAuthFile), {
    recursive: true,
  });

  await page.goto(
    './login?returnUrl=%2Fprofile',
  );

  const loginForm = page.locator('app-login form');
  const usernameInput = loginForm.locator('input#username');
  const passwordInput = loginForm.locator('input#password');
  const loginButton = loginForm.locator('button');

  await expect(usernameInput).toBeVisible();
  await expect(passwordInput).toBeVisible();
  await expect(loginButton).toBeVisible();

  await usernameInput.fill(candidateUsername);
  await passwordInput.fill(candidatePassword);

  const loginResponsePromise = page.waitForResponse(response => {
    return (
      response.request().method() === 'POST' &&
      response.url().endsWith('/api/portal/auth/login')
    );
  });

  await loginButton.click();

  const loginResponse = await loginResponsePromise;

  if (!loginResponse.ok()) {
    const responseBody = await loginResponse.text();

    throw new Error(
      `Candidate login failed with HTTP ${loginResponse.status()}. ` +
      `Response: ${responseBody.slice(0, 500)}`,
    );
  }

  /*
   * AuthenticationService stores these values through LocalStorageService.
   * LocalStorageService prefixes every key with "tc-candidate-".
   */
  await expect
  .poll(
    async () => {
      return page.evaluate(() => {
        const token = localStorage.getItem(
          'tc-candidate-access-token',
        );

        const user = localStorage.getItem(
          'tc-candidate-user',
        );

        const rawInstanceType = localStorage.getItem(
          'tc-candidate-tc_instance_type',
        );

        let instanceType: string | null = null;

        if (rawInstanceType) {
          try {
            instanceType = JSON.parse(rawInstanceType) as string;
          } catch {
            instanceType = null;
          }
        }

        return {
          hasToken: Boolean(token),
          hasUser: Boolean(user),
          instanceType,
        };
      });
    },
    {
      message:
        'Expected successful login to populate the candidate session',
      timeout: 15_000,
    },
  )
  .toEqual({
    hasToken: true,
    hasUser: true,
    instanceType: 'GRN',
  });

  await page.context().storageState({
    path: candidateAuthFile,
  });
});
