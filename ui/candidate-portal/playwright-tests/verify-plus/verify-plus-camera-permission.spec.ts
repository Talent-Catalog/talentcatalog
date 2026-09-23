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
  installCameraPermissionDeniedMock,
} from '../support/media-devices.mock';

test.describe('Verify+ camera permission', () => {
  test.beforeEach(async ({page}) => {
    /*
     * This must run before goto(), because application code must see the
     * mocked MediaDevices API from the beginning of page execution.
     */
    await installCameraPermissionDeniedMock(page);
  });

  test(
    'shows permission-denied guidance and allows another attempt',
    async ({page}) => {
      const verifyPlusPage = new VerifyPlusPage(page);

      await test.step('Open Verify+', async () => {
        await verifyPlusPage.gotoServices();
        await verifyPlusPage.openVerifyPlus();
      });

      await test.step('Deny the initial camera request', async () => {
        await verifyPlusPage.enableCamera();

        await expect(
          verifyPlusPage.permissionDeniedMessage,
        ).toBeVisible();

        await expect(
          verifyPlusPage.tryAgainButton,
        ).toBeVisible();

        await expect(
          verifyPlusPage.enableCameraButton,
        ).not.toBeVisible();

        await expect
        .poll(
          () => getUserMediaCallCount(page),
          {
            message:
              'Expected one camera request after clicking Enable camera',
          },
        )
        .toBe(1);
      });

      await test.step('Retry camera access', async () => {
        await verifyPlusPage.retryCamera();

        await expect(
          verifyPlusPage.permissionDeniedMessage,
        ).toBeVisible();

        await expect(
          verifyPlusPage.tryAgainButton,
        ).toBeVisible();

        await expect
        .poll(
          () => getUserMediaCallCount(page),
          {
            message:
              'Expected another camera request after clicking Try again',
          },
        )
        .toBe(2);
      });

      await test.step('Return to Services', async () => {
        await verifyPlusPage.returnToServices();
      });
    },
  );
});
