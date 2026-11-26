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
import {performance} from 'node:perf_hooks';
import {resolve} from 'node:path';
import {isCandidateDetailsApi, readCandidateFixture} from './candidate-details-metrics';
import {
  attachJourneyResult,
  ensureOk,
  JourneyApiCapture,
  newRun,
  pathOf,
  REPEATS,
  roundMs,
  WAIT_MS,
} from './performance-journey-utils';
import type {MeasuredRun} from './performance-journey-utils';

// Created by the existing warm-up project before the chromium journey runs.
test.use({
  storageState: resolve(__dirname, '../../../.auth/perf-user.json'),
});

test('Candidate Details performance journey', async ({page}, testInfo) => {
  test.setTimeout(360_000);

  const runs: MeasuredRun[] = [];
  try {
    const candidateNumber = readCandidateFixture(process.env);
    const profilePath = `/api/admin/candidate/number/${candidateNumber}`;

    for (let repetition = 1; repetition <= REPEATS; repetition++) {
      // Reset the browser page between runs. Never time the reset or warm-up.
      await page.goto('about:blank');
      const run = newRun(repetition);
      runs.push(run);
      const capture = new JourneyApiCapture(page, run, isCandidateDetailsApi);

      const profileResponsePromise = page.waitForResponse(response =>
          response.request().method() === 'GET' &&
          pathOf(response.url()) === profilePath,
      {timeout: WAIT_MS});
      void profileResponsePromise.catch(() => {});

      let elapsedMs: number | null = null;
      capture.start();
      try {
        const start = performance.now();
        const navigation = await page.goto(
            `candidate/${encodeURIComponent(candidateNumber)}`,
            {waitUntil: 'domcontentloaded', timeout: WAIT_MS}
        );
        if (!navigation?.ok()) {
          throw new Error(
              '[perf-candidate-details] Candidate route returned HTTP ' +
              (navigation?.status() ?? 'no response')
          );
        }

        const response = await profileResponsePromise;
        await ensureOk(response);
        const body = await response.json() as {candidateNumber?: string | number} | null;
        if (String(body?.candidateNumber ?? '') !== candidateNumber) {
          throw new Error(
              '[perf-candidate-details] The profile API did not return the ' +
              'configured staging candidate.'
          );
        }

        await expect(page).toHaveURL(
            new RegExp(`/admin-portal/candidate/${candidateNumber}(?:[?#]|$)`),
            {timeout: WAIT_MS}
        );
        await expect(
            page.locator('app-view-candidate .candidate-header-container p')
        ).toContainText(`Candidate #${candidateNumber}`, {timeout: WAIT_MS});
        await expect(
            page.locator('app-view-candidate .candidate-name-container h2')
        ).toBeVisible({timeout: WAIT_MS});
        await expect(
            page.locator('app-view-candidate tc-tabs')
        ).toBeVisible({timeout: WAIT_MS});

        // Stop at visible profile content; API capture cleanup is not timed.
        elapsedMs = roundMs(performance.now() - start);
      } finally {
        await capture.stop();
      }

      capture.assertSuccessful([{method: 'GET', path: profilePath}]);
      if (elapsedMs === null) {
        throw new Error('[perf-candidate-details] Duration was not measured');
      }
      run.durationMs = elapsedMs;
    }
  } finally {
    await attachJourneyResult(testInfo, 'candidate-details',
        'candidate-details-performance.json', runs);
  }
});
