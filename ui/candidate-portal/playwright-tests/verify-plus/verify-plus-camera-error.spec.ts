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

import {expect, test} from '@playwright/test';

import {VerifyPlusPage} from '../pages/verify-plus.page';
import {
  getUserMediaCallCount,
  installUnexpectedCameraErrorMock,
} from '../support/media-devices.mock';

test.describe('Verify+ unexpected camera error', () => {
  test.beforeEach(async ({page}) => {
    /*
     * Installing the browser API mock before navigation so all application code
     * sees the deterministic MediaDevices implementation.
     */
    await installUnexpectedCameraErrorMock(page);
  });

  test(
    'shows a generic scanner error and allows another camera attempt',
    async ({page}) => {
      const verifyPlusPage = new VerifyPlusPage(page);

      await test.step('Open Verify+', async () => {
        await verifyPlusPage.gotoServices();
        await verifyPlusPage.openVerifyPlus();
      });

      await test.step(
        'Simulate an unexpected camera failure',
        async () => {
          await verifyPlusPage.enableCamera();

          await expect(
            verifyPlusPage.scannerErrorMessage,
          ).toBeVisible();

          await expect(
            verifyPlusPage.permissionDeniedMessage,
          ).not.toBeVisible();

          await expect(
            verifyPlusPage.noCameraMessage,
          ).not.toBeVisible();

          await expect(
            verifyPlusPage.tryAgainButton,
          ).not.toBeVisible();

          /*
           * An unexpected failure resets the scanner to its initial state,
           * allowing the candidate to request camera access again.
           */
          await expect(
            verifyPlusPage.enableCameraButton,
          ).toBeVisible();

          await expect(
            verifyPlusPage.enableCameraButton,
          ).toBeEnabled();

          await expect
          .poll(
            () => getUserMediaCallCount(page),
            {
              message:
                'Expected one getUserMedia request after enabling the camera',
            },
          )
          .toBe(1);
        },
      );

      await test.step(
        'Allow the candidate to retry',
        async () => {
          await verifyPlusPage.enableCamera();

          await expect(
            verifyPlusPage.scannerErrorMessage,
          ).toBeVisible();

          await expect(
            verifyPlusPage.enableCameraButton,
          ).toBeVisible();

          await expect
          .poll(
            () => getUserMediaCallCount(page),
            {
              message:
                'Expected a second getUserMedia request after retrying',
            },
          )
          .toBe(2);
        },
      );

      await test.step('Return to Services', async () => {
        await verifyPlusPage.returnToServices();
      });
    },
  );
});
