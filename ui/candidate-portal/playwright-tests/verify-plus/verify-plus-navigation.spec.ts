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

test.describe('Verify+ navigation', () => {
  test(
    'authenticated GRN candidate can open Verify+ and return to Services',
    async ({page}) => {
      const verifyPlusPage = new VerifyPlusPage(page);

      await test.step('Open the candidate Services tab', async () => {
        await verifyPlusPage.gotoServices();
      });

      await test.step('Open the Verify+ feature', async () => {
        await verifyPlusPage.openVerifyPlus();
      });

      await test.step('Verify the initial scanner state', async () => {
        await expect(verifyPlusPage.title).toBeVisible();

        await expect(
          verifyPlusPage.description,
        ).toBeVisible();

        await expect(
          verifyPlusPage.scanner,
        ).toBeVisible();

        await expect(
          verifyPlusPage.enableCameraButton,
        ).toBeVisible();

        await expect(
          verifyPlusPage.enableCameraButton,
        ).toBeEnabled();

        await expect(
          verifyPlusPage.backButton,
        ).toBeVisible();
      });

      await test.step('Return to the Services list', async () => {
        await verifyPlusPage.returnToServices();
      });
    },
  );
});
