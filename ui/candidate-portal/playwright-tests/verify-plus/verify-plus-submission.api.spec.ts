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

import {randomInt} from 'node:crypto';
import {APIRequestContext, APIResponse} from '@playwright/test';

import {expect, test} from '../fixtures/verify-plus-api.fixture';
import {
  getE2EVerifyPlusBaselineUnhcrId,
  getE2EVerifyPlusDuplicateUnhcrId
} from "../support/e2e-env";

/**
 * Authenticated Verify+ submission endpoint.
 */
const VERIFY_PLUS_ENDPOINT =
  '/api/portal/verify-plus';

interface VerifyPlusResponse {
  unhcrNumber: string;
  duplicate: boolean;
}

interface PortalErrorResponse {
  timestamp: number;
  code: string;
  message: string;
  data?: unknown;
}

/**
 * Creates a unique synthetic UNHCR identifier using the same general format
 * as the Verify+ development fixtures.
 *
 * The identifier is intentionally unique because a successful Verify+
 * submission writes it to the authenticated candidate.
 *
 * @returns synthetic UNHCR identifier
 */
function createUniqueUnhcrId(): string {
  const timestamp = Date.now()
  .toString()
  .padStart(13, '0');

  const prefix = timestamp.slice(-10, -7);
  const middle = timestamp.slice(-7, -5);
  const suffix = timestamp.slice(-5);

  const letter = String.fromCharCode(
    'A'.charCodeAt(0) + randomInt(0, 26),
  );

  return `${prefix}-${middle}${letter}${suffix}`;
}

/**
 * Submits a mock-1 Verify+ payload.
 *
 * @param api authenticated candidate-portal API context
 * @param unhcrId UNHCR identifier encoded in the synthetic payload
 * @returns Verify+ endpoint response
 */
async function submitVerifyPlusPayload(
  api: APIRequestContext,
  unhcrId: string,
): Promise<APIResponse> {
  const rawPayload = JSON.stringify({
    v: 'mock-1',
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

async function restoreBaselineUnhcrId(
  api: APIRequestContext,
  baselineUnhcrId: string,
): Promise<void> {
  const response =
    await submitVerifyPlusPayload(
      api,
      baselineUnhcrId,
    );

  expect.soft(
    response.status(),
    'Expected Verify+ test cleanup to restore the baseline UNHCR number',
  ).toBe(200);
}
/**
 * Asserts the standard portal response for an invalid Verify+ payload.
 *
 * @param response Verify+ API response
 * @param expectedMessage expected backend validation message
 */
async function expectInvalidRequest(
  response: APIResponse,
  expectedMessage: string,
): Promise<void> {
  expect(response.status()).toBe(400);

  const body =
    await response.json() as PortalErrorResponse;

  expect(body).toMatchObject({
    code: 'invalid_request',
    message: expectedMessage,
  });

  expect(body.timestamp).toEqual(
    expect.any(Number),
  );
}

test.describe('Verify+ submission API', () => {
  /*
   * These tests share one authenticated candidate. Keeping them serial makes
   * future additions safe even if project-level worker counts are increased.
   */
  test.describe.configure({
    mode: 'serial',
  });

  test(
    'accepts a valid mock-1 payload',
    async ({verifyPlusApi}) => {
      const baselineUnhcrId =
        getE2EVerifyPlusBaselineUnhcrId();

      const unhcrId = createUniqueUnhcrId();

      try {
        const response =
          await submitVerifyPlusPayload(
            verifyPlusApi,
            unhcrId,
          );

        expect(
          response.status(),
          `Unexpected response from ${response.url()}`,
        ).toBe(200);

        const body =
          await response.json() as VerifyPlusResponse;

        expect(body).toEqual({
          unhcrNumber: unhcrId,
          duplicate: false,
        });
      } finally {
        await restoreBaselineUnhcrId(
          verifyPlusApi,
          baselineUnhcrId,
        );
      }
    },
  );

  test(
    'reports a UNHCR number owned by another active candidate as duplicate',
    async ({verifyPlusApi}) => {
      const duplicateUnhcrId =
        getE2EVerifyPlusDuplicateUnhcrId();
      const baselineUnhcrId =
        getE2EVerifyPlusBaselineUnhcrId();
      if (!duplicateUnhcrId) {
        test.skip(
          true,
          'E2E_VERIFY_PLUS_DUPLICATE_UNHCR_ID is not configured.',
        );

        return;
      }

      try {
        const response =
          await submitVerifyPlusPayload(
            verifyPlusApi,
            duplicateUnhcrId,
          );

        expect(
          response.status(),
          `Unexpected duplicate response from ${response.url()}`,
        ).toBe(200);

        const body =
          await response.json() as VerifyPlusResponse;


        expect(body).toEqual({
          unhcrNumber: duplicateUnhcrId,
          duplicate: true,
        });
      } finally {
        await restoreBaselineUnhcrId(
          verifyPlusApi,
          baselineUnhcrId,
        );
      }
    },
  );

  test(
    'rejects a blank raw payload',
    async ({verifyPlusApi}) => {
      const response =
        await verifyPlusApi.post(VERIFY_PLUS_ENDPOINT, {
          data: {
            rawPayload: '   ',
            consented: true,
          },
        });

      expect(response.status()).toBe(400);

      const body =
        await response.json() as PortalErrorResponse;

      expect(body).toMatchObject({
        code: 'validation_error',
        message: 'Raw payload is required',
      });

      expect(body.timestamp).toEqual(
        expect.any(Number),
      );
    },
  );

  test(
    'rejects malformed JSON',
    async ({verifyPlusApi}) => {
      const response =
        await verifyPlusApi.post(VERIFY_PLUS_ENDPOINT, {
          data: {
            rawPayload:
              'this is not a JSON payload',
            consented: true,
          },
        });

      await expectInvalidRequest(
        response,
        'Malformed Verify+ payload',
      );
    },
  );

  test(
    'rejects an unsupported payload version',
    async ({verifyPlusApi}) => {
      const rawPayload = JSON.stringify({
        v: 'mock-2',
        unhcrId: createUniqueUnhcrId(),
      });

      const response =
        await verifyPlusApi.post(VERIFY_PLUS_ENDPOINT, {
          data: {
            rawPayload,
            consented: true,
          },
        });

      await expectInvalidRequest(
        response,
        'Unsupported Verify+ payload version: mock-2',
      );
    },
  );
});
