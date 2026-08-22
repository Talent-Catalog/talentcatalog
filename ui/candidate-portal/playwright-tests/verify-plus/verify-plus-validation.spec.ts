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

import type {Response as PlaywrightResponse,} from '@playwright/test';

import {expect, test,} from '../fixtures/verify-plus-api.fixture';

import {
  VERIFY_PLUS_VALIDATION_QR_FIXTURES,
  VerifyPlusQrValidationFixture,
} from '../fixtures/verify-plus-qr.fixture';

import {VerifyPlusPage,} from '../pages/verify-plus.page';

import {
  getE2EVerifyPlusBaselineUnhcrId,
  getE2EVerifyPlusDuplicateUnhcrId,
} from '../support/e2e-env';

import {restoreVerifyPlusBaselineUnhcrId,} from '../support/verify-plus-api';

import {scanVerifyPlusQrFixtureToReview,} from '../support/verify-plus-qr-decoding.e2e';

/**
 * Standard guidance displayed underneath Verify+ backend validation errors.
 */
const SUBMISSION_ERROR_HINT =
  'If this QR code is a valid UNHCR Verify+ code, please rescan.';

/**
 * Returns whether a validation scenario may modify persistent candidate data.
 *
 * Both successful and duplicate submissions reach the backend ingest path and
 * can update the authenticated candidate's UNHCR number. The test therefore
 * restores the configured baseline after either scenario.
 *
 * Validation-error scenarios fail before the candidate is updated, so they do
 * not require cleanup.
 *
 * @param fixture Verify+ QR validation fixture
 * @returns true when candidate cleanup is required
 */
function requiresCandidateCleanup(
  fixture: VerifyPlusQrValidationFixture,
): boolean {
  return (
    fixture.validation.kind ===
    'success' ||
    fixture.validation.kind ===
    'duplicate'
  );
}

/**
 * Checks whether environment-backed data supports the current validation
 * fixture.
 *
 * The valid fixture must match the authenticated candidate's configured
 * baseline UNHCR number.
 *
 * Duplicate validation requires the static PNG's UNHCR number to exist on
 * another active-like candidate. If the configured duplicate record uses a
 * different identifier, this browser scenario is skipped because the current
 * environment cannot exercise that exact end-to-end path.
 *
 * Error fixtures do not depend on existing database state.
 *
 * @param fixture QR validation fixture about to be submitted
 */
function prepareValidationFixture(
  fixture: VerifyPlusQrValidationFixture,
): void {
  const validation =
    fixture.validation;

  switch (
    validation.kind
    ) {
    case 'success': {
      const baselineUnhcrId =
        getE2EVerifyPlusBaselineUnhcrId();

      expect(
        validation.unhcrId,
        `${fixture.fileName} must encode the configured ` +
        'E2E_VERIFY_PLUS_BASELINE_UNHCR_ID',
      ).toBe(
        baselineUnhcrId,
      );

      return;
    }

    case 'duplicate': {
      const configuredDuplicateUnhcrId =
        getE2EVerifyPlusDuplicateUnhcrId();

      if (
        !configuredDuplicateUnhcrId
      ) {
        test.skip(
          true,
          'Duplicate browser validation requires ' +
          'E2E_VERIFY_PLUS_DUPLICATE_UNHCR_ID.',
        );

        return;
      }

      if (
        configuredDuplicateUnhcrId !==
        validation.unhcrId
      ) {
        test.skip(
          true,
          `${fixture.fileName} decodes to ${validation.unhcrId}, ` +
          `but this environment's seeded duplicate is ` +
          `${configuredDuplicateUnhcrId}. ` +
          'The static PNG and database cannot be changed, so this exact ' +
          'browser-to-backend duplicate scenario cannot be exercised here.',
        );

        return;
      }

      return;
    }

    case 'error':
      return;
  }
}
/**
 * Confirms the decoded QR and waits for the real browser Verify+ submission
 * response.
 *
 * The response listener is created before Confirm is clicked so even a very
 * fast backend response cannot be missed.
 *
 * This allows the validation test to independently verify:
 *
 * 1. the HTTP/backend result;
 * 2. the resulting Verify+ browser UI.
 *
 * @param verifyPlusPage Verify+ page positioned on the review screen
 * @returns Playwright browser response from the Verify+ submission endpoint
 */
async function confirmAndWaitForResponse(
  verifyPlusPage: VerifyPlusPage,
): Promise<PlaywrightResponse> {
  const responsePromise =
    verifyPlusPage.page.waitForResponse(
      response => {
        const request =
          response.request();

        return (
          request.method() ===
          'POST' &&
          response.url().includes(
            '/api/portal/verify-plus',
          )
        );
      },
      {
        timeout:
          30_000,
      },
    );

  /*
   * Start listening before clicking Confirm.
   */
  await verifyPlusPage
  .confirmScan();

  return responsePromise;
}

/**
 * Verifies both the backend response and the rendered Verify+ result after the
 * user confirms a decoded QR payload.
 *
 * QR decoding itself has already been verified by
 * {@link scanVerifyPlusQrFixtureToReview}. These assertions are concerned only
 * with submission and business validation.
 *
 * @param verifyPlusPage submitted Verify+ page
 * @param fixture submitted QR fixture
 * @param response real browser response from the Verify+ endpoint
 */
async function expectValidationResult(
  verifyPlusPage: VerifyPlusPage,
  fixture: VerifyPlusQrValidationFixture,
  response: PlaywrightResponse,
): Promise<void> {
  const validation =
    fixture.validation;

  switch (
    validation.kind
    ) {
    case 'success': {
      /*
       * A valid Verify+ submission should succeed and report that the UNHCR
       * number is not owned by another active-like candidate.
       */
      expect(
        response.status(),
        `Expected ${fixture.fileName} submission to succeed`,
      ).toBe(
        200,
      );

      const body =
        await response.json() as {
          unhcrNumber: string;
          duplicate: boolean;
        };

      expect(
        body,
        `Expected ${fixture.fileName} to return a non-duplicate result`,
      ).toEqual({
        unhcrNumber:
        validation.unhcrId,

        duplicate:
          false,
      });

      await expect(
        verifyPlusPage
          .successHeading,
        `Expected ${fixture.fileName} to show the success result`,
      ).toBeVisible({
        timeout:
          15_000,
      });

      await expect(
        verifyPlusPage
          .successBody,
      ).toContainText(
        'Your UNHCR number was captured successfully:',
      );

      await expect(
        verifyPlusPage
          .successBody,
      ).toContainText(
        validation.unhcrId,
      );

      await expect(
        verifyPlusPage
          .submissionError,
      ).not.toBeVisible();

      return;
    }

    case 'duplicate': {
      /*
       * Duplicate detection is still a successful Verify+ submission. The
       * backend returns HTTP 200 with duplicate=true.
       */
      expect(
        response.status(),
        `Expected ${fixture.fileName} duplicate submission to succeed`,
      ).toBe(
        200,
      );

      const body =
        await response.json() as {
          unhcrNumber: string;
          duplicate: boolean;
        };

      expect(
        body,
        `Expected ${fixture.fileName} to return a duplicate result`,
      ).toEqual({
        unhcrNumber:
        validation.unhcrId,

        duplicate:
          true,
      });

      await expect(
        verifyPlusPage
          .duplicateHeading,
        `Expected ${fixture.fileName} to display the duplicate result`,
      ).toBeVisible({
        timeout:
          15_000,
      });

      await expect(
        verifyPlusPage
          .duplicateBody,
      ).toContainText(
        validation.unhcrId,
      );

      await expect(
        verifyPlusPage
          .duplicateRescanHint,
      ).toBeVisible();

      await expect(
        verifyPlusPage
          .submissionError,
      ).not.toBeVisible();

      return;
    }

    case 'error': {
      /*
       * The QR itself was valid enough to decode, but the decoded payload does
       * not satisfy the Verify+ backend contract.
       */
      expect(
        response.status(),
        `Expected ${fixture.fileName} to be rejected`,
      ).toBe(
        400,
      );

      const body =
        await response.json() as {
          code: string;
          message: string;
        };

      expect(
        body.message,
        `Unexpected validation message for ${fixture.fileName}`,
      ).toBe(
        validation.message,
      );

      await expect(
        verifyPlusPage
          .submissionError,
        `Expected ${fixture.fileName} validation error to appear`,
      ).toBeVisible({
        timeout:
          15_000,
      });

      await expect(
        verifyPlusPage
          .submissionError,
      ).toContainText(
        validation.message,
      );

      await expect(
        verifyPlusPage
          .submissionError,
      ).toContainText(
        SUBMISSION_ERROR_HINT,
      );

      /*
       * Rejected payloads remain available for review so the candidate can
       * either retry or choose Rescan.
       */
      await expect(
        verifyPlusPage
          .payloadReviewHeading,
      ).toBeVisible();

      await expect(
        verifyPlusPage
          .confirmButton,
      ).toBeVisible();

      await expect(
        verifyPlusPage
          .rescanButton,
      ).toBeVisible();

      return;
    }
  }
}

/**
 * Verify+ browser validation/submission coverage.
 *
 * Unlike decoder-only tests, this suite continues beyond the review screen and
 * clicks Confirm. This exercises the real browser-to-backend submission flow.
 *
 * Flow:
 *
 * PNG fixture
 * -> deterministic camera
 * -> real zxing-wasm decoding
 * -> decoded-payload review
 * -> Confirm
 * -> real Verify+ HTTP request
 * -> backend payload validation
 * -> success / duplicate / validation-error UI
 *
 * Successful and duplicate scenarios may update shared candidate data. The
 * candidate is restored to its configured baseline UNHCR number in a finally
 * block.
 *
 * Tests are not configured with Playwright `serial` mode. The project already
 * uses one worker for this shared candidate, while normal test execution allows
 * later validation cases to continue even if one scenario fails.
 */
test.describe(
  'Verify+ payload validation',
  () => {
    for (
      const fixture
      of VERIFY_PLUS_VALIDATION_QR_FIXTURES
      ) {
      test(
        `validates ${fixture.fileName}`,
        async ({
                 page,
                 verifyPlusApi,
               }, testInfo) => {
          test.setTimeout(
            120_000,
          );

          /*
           * Validate environment-dependent fixture assumptions before starting
           * the relatively expensive browser scan.
           */
          prepareValidationFixture(
            fixture,
          );

          const requiresCleanup =
            requiresCandidateCleanup(
              fixture,
            );

          const baselineUnhcrId =
            requiresCleanup
              ? getE2EVerifyPlusBaselineUnhcrId()
              : null;

          try {
            /*
             * Reuse the same real decoder path as the decoder E2E tests and
             * stop once the decoded payload is ready for review.
             */
            const verifyPlusPage =
              await scanVerifyPlusQrFixtureToReview(
                page,
                testInfo.config.rootDir,
                fixture,
              );

            /*
             * Wait for the production submission response while Confirm is
             * clicked.
             */
            const response =
              await test.step(
                'Confirm decoded payload',
                async () => {
                  return confirmAndWaitForResponse(
                    verifyPlusPage,
                  );
                },
              );

            /*
             * Assert both the HTTP contract and what the candidate sees.
             */
            await test.step(
              'Verify submission result',
              async () => {
                await expectValidationResult(
                  verifyPlusPage,
                  fixture,
                  response,
                );
              },
            );
          } finally {
            /*
             * Successful and duplicate requests may modify the candidate's
             * stored UNHCR value. Always restore the known baseline even when a
             * later UI assertion fails.
             */
            if (
              requiresCleanup &&
              baselineUnhcrId
            ) {
              await restoreVerifyPlusBaselineUnhcrId(
                verifyPlusApi,
                baselineUnhcrId,
              );
            }
          }
        },
      );
    }
  },
);
