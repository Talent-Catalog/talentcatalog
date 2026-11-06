/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 *  the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {test} from '@playwright/test';
import {mkdir} from 'node:fs/promises';
import {dirname, resolve} from 'node:path';
import {warmUpAdminPortal} from './warmup';

test.setTimeout(240_000);

test('warm up staging before performance measurements', async ({page}) => {
  await warmUpAdminPortal(page);

  // Save the authenticated browser session.
  // Performance journeys can reuse it without measuring login.
  const authFile = resolve(__dirname, '../../.auth/perf-user.json');

  await mkdir(dirname(authFile), {recursive: true});
  await page.context().storageState({path: authFile});
});
