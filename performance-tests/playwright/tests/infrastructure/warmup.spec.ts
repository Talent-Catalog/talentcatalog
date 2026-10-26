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

import {expect, test} from '@playwright/test';
import {fillSensitiveInput, readWarmUpCredentials, warmUpStep} from '../warmup/warmup';

test('warm-up requires staging credentials', () => {
  expect(() => readWarmUpCredentials({}))
  .toThrow(/Missing PLAYWRIGHT_PERF_USERNAME/);
});

test('warm-up preserves provided credentials', () => {
  const result = readWarmUpCredentials({
    PLAYWRIGHT_PERF_USERNAME: 'staging-user',
    PLAYWRIGHT_PERF_PASSWORD: 'test-password',
  });

  expect(result.username).toBe('staging-user');
  expect(result.password).toBe('test-password');
});

test('warm-up failure identifies the failed step', async () => {
  await expect(
      warmUpStep('Candidate Search', async () => {
        throw new Error('HTTP 500');
      })
  ).rejects.toThrow(
      '[perf-warmup] Candidate Search failed: HTTP 500'
  );
});


test('sensitive input updates the form correctly', async ({page}) => {
  await page.setContent(`
    <tc-input formcontrolname="password">
      <input type="password">
    </tc-input>
    <output id="status"></output>

    <script>
      document.querySelector('input')
        .addEventListener('input', () => {
          document.querySelector('#status').textContent =
            'input event received';
        });
    </script>
  `);

  await fillSensitiveInput(
      page,
      'password',
      'example-test-secret'
  );

  await expect(
      page.locator('#status')
  ).toHaveText('input event received');
});

