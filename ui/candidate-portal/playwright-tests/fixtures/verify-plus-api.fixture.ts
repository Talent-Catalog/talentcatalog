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

import {
  APIRequestContext,
  expect,
  request as playwrightRequest,
  test as base
} from '@playwright/test';

import {readCandidateAccessToken} from '../support/auth-state';

import {getE2EApiBaseUrl} from '../support/e2e-env';

interface VerifyPlusApiFixtures {
  verifyPlusApi: APIRequestContext;
}

/**
 * Playwright test fixture providing an authenticated portal API client.
 *
 * The browser login setup stores its JWT in local storage. APIRequestContext
 * does not execute the Angular JWT interceptor, so this fixture reads that JWT
 * from candidate.json and supplies the equivalent Bearer header.
 */
export const test =
  base.extend<VerifyPlusApiFixtures>({
    verifyPlusApi: async ({}, use) => {
      const accessToken =
        readCandidateAccessToken();

      const apiContext =
        await playwrightRequest.newContext({
          baseURL: getE2EApiBaseUrl(),

          extraHTTPHeaders: {
            Accept: 'application/json',
            Authorization:
              `Bearer ${accessToken}`,
          },
        });

      try {
        await use(apiContext);
      } finally {
        await apiContext.dispose();
      }
    },
  });

export {expect};
