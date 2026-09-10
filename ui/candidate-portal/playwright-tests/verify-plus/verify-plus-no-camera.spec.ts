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
import {getUserMediaCallCount, installNoCameraDevicesMock,} from '../support/media-devices.mock';

test.describe('Verify+ camera availability', () => {
  test.beforeEach(async ({page}) => {
    /*
     * Install the mock before navigating so the Angular application sees the
     * deterministic MediaDevices implementation from initial page execution.
     */
    await installNoCameraDevicesMock(page);
  });

  test(
    'shows guidance when no camera device is available',
    async ({page}) => {
      const verifyPlusPage = new VerifyPlusPage(page);

      await test.step('Open Verify+', async () => {
        await verifyPlusPage.gotoServices();
        await verifyPlusPage.openVerifyPlus();
      });

      await test.step('Request camera access', async () => {
        await verifyPlusPage.enableCamera();
      });

      await test.step(
        'Show the no-camera state',
        async () => {
          await expect(
            verifyPlusPage.noCameraMessage,
          ).toBeVisible();

          await expect(
            verifyPlusPage.enableCameraButton,
          ).not.toBeVisible();

          await expect(
            verifyPlusPage.tryAgainButton,
          ).not.toBeVisible();

          await expect(
            verifyPlusPage.permissionDeniedMessage,
          ).not.toBeVisible();
        },
      );

      await test.step(
        'Do not request a media stream',
        async () => {
          await expect
          .poll(
            () => getUserMediaCallCount(page),
            {
              message:
                'Expected no getUserMedia request when no camera exists',
            },
          )
          .toBe(0);
        },
      );

      await test.step('Return to Services', async () => {
        await verifyPlusPage.returnToServices();
      });
    },
  );
});
