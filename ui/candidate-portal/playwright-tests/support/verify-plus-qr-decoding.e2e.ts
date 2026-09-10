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

import {expect, Page, test,} from '@playwright/test';

import {VerifyPlusQrFixture,} from '../fixtures/verify-plus-qr.fixture';

import {VerifyPlusPage,} from '../pages/verify-plus.page';

import {getQrCameraDiagnostics, installQrCameraMock,} from './qr-camera.mock';

import {expectVerifyPlusDecodedFixture, loadVerifyPlusQrFixtureAsDataUrl,} from './qr-fixture';

/**
 * Executes the complete browser-side Verify+ QR decoding journey for one
 * reusable PNG fixture.
 *
 * The helper intentionally stops before Confirm. Its responsibility is to
 * prove browser-side decoding independently from backend submission and
 * payload-validation behavior.
 *
 * Test flow:
 *
 * fixture PNG
 * -> synthetic camera
 * -> production scanner interval
 * -> production capture canvas
 * -> browser ImageData
 * -> VerifyPlusDecoderService
 * -> zxing-wasm/WASM
 * -> Angular scanned event
 * -> decoded-payload UI
 *
 * The same helper can therefore be reused by simple synthetic fixtures,
 * malformed/non-JSON fixtures and high-density QR images.
 *
 * @param page active Playwright page
 * @param playwrightRootDir Playwright test root directory
 * @param fixture QR fixture to decode
 */
export async function runVerifyPlusQrDecodingScenario(
  page: Page,
  playwrightRootDir: string,
  fixture: VerifyPlusQrFixture,
): Promise<void> {
  const verifyPlusPage =
    new VerifyPlusPage(
      page,
    );

  const qrPngDataUrl =
    await loadVerifyPlusQrFixtureAsDataUrl(
      playwrightRootDir,
      fixture,
    );

  await test.step(
    'Open Verify+',
    async () => {
      await verifyPlusPage
      .gotoServices();

      await verifyPlusPage
      .openVerifyPlus();
    },
  );

  await test.step(
    `Install ${fixture.fileName} as the camera frame`,
    async () => {
      await installQrCameraMock(
        page,
        qrPngDataUrl,
      );
    },
  );

  await test.step(
    'Capture the QR frame',
    async () => {
      await verifyPlusPage
      .enableCamera();

      /*
       * This assertion distinguishes camera/frame failures from decoder
       * failures. If it passes, production captureFrame() consumed at least one
       * deterministic image.
       */
      await expect
      .poll(
        async () => {
          const diagnostics =
            await getQrCameraDiagnostics(
              page,
            );

          return diagnostics
            .injectedFrames;
        },
        {
          message:
            `Expected Verify+ to capture ${fixture.fileName}`,

          timeout:
            15_000,
        },
      )
      .toBeGreaterThan(
        0,
      );
    },
  );

  await test.step(
    'Decode the QR with the production decoder',
    async () => {
      /*
       * The review UI appears only after the real scanner has emitted a
       * successfully decoded string.
       */
      await expect(
        verifyPlusPage
          .payloadReviewHeading,

        `Expected zxing-wasm to decode ${fixture.fileName}`,
      ).toBeVisible({
        timeout:
          30_000,
      });

      const decodedPayload =
        await verifyPlusPage
        .payloadPreview
        .innerText();

      expectVerifyPlusDecodedFixture(
        decodedPayload,
        fixture,
      );
    },
  );

  await test.step(
    'Show decoded-payload actions',
    async () => {
      await expect(
        verifyPlusPage
          .consentCheckbox,
      ).toBeVisible();

      await expect(
        verifyPlusPage
          .confirmButton,
      ).toBeVisible();

      await expect(
        verifyPlusPage
          .confirmButton,
      ).toBeDisabled();

      await expect(
        verifyPlusPage
          .rescanButton,
      ).toBeVisible();

      await expect(
        verifyPlusPage
          .rescanButton,
      ).toBeEnabled();

      await expect(
        verifyPlusPage
          .scannerErrorMessage,
      ).not.toBeVisible();
    },
  );

  await test.step(
    'Return to Services',
    async () => {
      /*
       * Confirm is deliberately not clicked. Payload acceptance/rejection is a
       * separate concern and can later reuse the same fixture catalog.
       */
      await verifyPlusPage
      .returnToServices();
    },
  );
}

/**
 * Scans one deterministic QR fixture and stops when Verify+ reaches the review
 * screen.
 *
 * This helper provides the shared first half of both decoder and validation
 * E2E tests:
 *
 * PNG
 * -> deterministic camera
 * -> production scanner
 * -> production canvas
 * -> ImageData
 * -> VerifyPlusDecoderService
 * -> zxing-wasm
 * -> Review scanned payload
 *
 * It intentionally does not click Confirm.
 *
 * @param page active Playwright browser page
 * @param playwrightRootDir Playwright test root
 * @param fixture QR image fixture to scan
 * @returns page object positioned at the payload review screen
 */
export async function scanVerifyPlusQrFixtureToReview(
  page: Page,
  playwrightRootDir: string,
  fixture: VerifyPlusQrFixture,
): Promise<VerifyPlusPage> {
  const verifyPlusPage =
    new VerifyPlusPage(
      page,
    );

  const qrPngDataUrl =
    await loadVerifyPlusQrFixtureAsDataUrl(
      playwrightRootDir,
      fixture,
    );

  await test.step(
    'Open Verify+',
    async () => {
      await verifyPlusPage
      .gotoServices();

      await verifyPlusPage
      .openVerifyPlus();
    },
  );

  await test.step(
    `Install ${fixture.fileName} as the camera frame`,
    async () => {
      await installQrCameraMock(
        page,
        qrPngDataUrl,
      );
    },
  );

  await test.step(
    `Scan ${fixture.fileName}`,
    async () => {
      await verifyPlusPage
      .enableCamera();

      await expect
      .poll(
        async () => {
          const diagnostics =
            await getQrCameraDiagnostics(
              page,
            );

          return diagnostics
            .injectedFrames;
        },
        {
          message:
            `Expected Verify+ to capture ${fixture.fileName}`,

          timeout:
            15_000,
        },
      )
      .toBeGreaterThan(
        0,
      );
    },
  );

  await test.step(
    'Reach decoded-payload review',
    async () => {
      await expect(
        verifyPlusPage
          .payloadReviewHeading,

        `Expected zxing-wasm to decode ${fixture.fileName}`,
      ).toBeVisible({
        timeout:
          30_000,
      });

      const decodedPayload =
        await verifyPlusPage
        .payloadPreview
        .innerText();

      expectVerifyPlusDecodedFixture(
        decodedPayload,
        fixture,
      );

      await expect(
        verifyPlusPage
          .consentCheckbox,
      ).toBeVisible();

      await expect(
        verifyPlusPage
          .confirmButton,
      ).toBeVisible();

      await expect(
        verifyPlusPage
          .confirmButton,
      ).toBeDisabled();
    },
  );

  return verifyPlusPage;
}
