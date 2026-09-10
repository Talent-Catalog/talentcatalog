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

import {APIRequestContext, APIResponse, expect,} from '@playwright/test';

/**
 * Authenticated candidate-portal Verify+ endpoint.
 */
export const VERIFY_PLUS_ENDPOINT =
  '/api/portal/verify-plus';

/**
 * Submits a synthetic mock-1 Verify+ payload directly through the authenticated
 * API context.
 *
 * This helper is primarily used for restoring shared E2E candidate state after
 * browser or API tests that successfully modify the candidate's UNHCR number.
 *
 * @param api authenticated candidate-portal API context
 * @param unhcrId UNHCR identifier to submit
 * @returns Verify+ API response
 */
export async function submitVerifyPlusMockPayload(
  api: APIRequestContext,
  unhcrId: string,
): Promise<APIResponse> {
  const rawPayload =
    JSON.stringify({
      v:
        'mock-1',

      unhcrId,
    });

  return api.post(
    VERIFY_PLUS_ENDPOINT,
    {
      data: {
        rawPayload,
        consented: true,
      },
    },
  );
}

/**
 * Restores the dedicated Playwright candidate to its configured baseline UNHCR
 * identifier.
 *
 * Successful Verify+ validation tests modify persistent backend state.
 * Restoring the baseline in a finally block prevents one test or browser run
 * from changing the starting conditions of later tests.
 *
 * @param api authenticated candidate-portal API context
 * @param baselineUnhcrId configured baseline UNHCR identifier
 */
export async function restoreVerifyPlusBaselineUnhcrId(
  api: APIRequestContext,
  baselineUnhcrId: string,
): Promise<void> {
  const response =
    await submitVerifyPlusMockPayload(
      api,
      baselineUnhcrId,
    );

  expect.soft(
    response.status(),
    'Expected Verify+ cleanup to restore the baseline UNHCR number',
  ).toBe(
    200,
  );
}


