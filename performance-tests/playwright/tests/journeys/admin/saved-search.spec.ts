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
import {isSavedSearchApi, readSavedSearchFixture} from './saved-search-metrics';
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

// Reuse the authenticated account produced by the existing warm-up project.
test.use({
  storageState: resolve(__dirname, '../../../.auth/perf-user.json'),
});

test('Saved Search performance journey', async ({page}, testInfo) => {
  test.setTimeout(360_000);

  const runs: MeasuredRun[] = [];
  let firstRequest: string | undefined;
  let firstFilters: string | undefined;

  try {
    const savedSearchId = readSavedSearchFixture(process.env);
    const metadataPath = `/api/admin/saved-search/${savedSearchId}`;
    const filtersPath = `${metadataPath}/load`;
    const resultsPath =
        `/api/admin/saved-search-candidate/${savedSearchId}/search-paged`;
    for (let repetition = 1; repetition <= REPEATS; repetition++) {
      // Full route navigation on every repetition; reset is not timed.
      await page.goto('about:blank');
      const run = newRun(repetition);
      runs.push(run);
      const capture = new JourneyApiCapture(page, run, isSavedSearchApi);

      // Register all response listeners *before* triggering navigation.
      const metadataPromise = page.waitForResponse(response =>
          response.request().method() === 'GET' &&
          pathOf(response.url()) === metadataPath,
      {timeout: WAIT_MS});
      const filtersPromise = page.waitForResponse(response =>
          response.request().method() === 'GET' &&
          pathOf(response.url()) === filtersPath,
      {timeout: WAIT_MS});
      const resultsPromise = page.waitForResponse(response =>
          response.request().method() === 'POST' &&
          pathOf(response.url()) === resultsPath,
      {timeout: WAIT_MS});
      void metadataPromise.catch(() => {});
      void filtersPromise.catch(() => {});
      void resultsPromise.catch(() => {});

      let elapsedMs: number | null = null;
      capture.start();
      try {
        const start = performance.now();
        const navigation = await page.goto(`search/${savedSearchId}`, {
          waitUntil: 'domcontentloaded',
          timeout: WAIT_MS,
        });
        if (!navigation?.ok()) {
          throw new Error(
              '[perf-saved-search] Saved Search route returned HTTP ' +
              (navigation?.status() ?? 'no response')
          );
        }

        const metadataResponse = await metadataPromise;
        await ensureOk(metadataResponse);
        const metadata = await metadataResponse.json() as {
          id?: number;
          defaultSearch?: boolean;
        } | null;
        if (Number(metadata?.id) !== savedSearchId ||
            metadata?.defaultSearch === true) {
          throw new Error(
              '[perf-saved-search] Fixture must be a stable, non-default ' +
              'Saved Search available to the performance account.'
          );
        }

        const filtersResponse = await filtersPromise;
        await ensureOk(filtersResponse);
        const filters = JSON.stringify(await filtersResponse.json());
        if (firstFilters === undefined) {
          firstFilters = filters;
        } else if (filters !== firstFilters) {
          // Never print the complete saved search filter payload into CI logs.
          throw new Error(
              '[perf-saved-search] Saved Search filters changed between repetitions'
          );
        }

        const resultsResponse = await resultsPromise;
        await ensureOk(resultsResponse);
        const request = JSON.stringify(
            resultsResponse.request().postDataJSON()
        );
        if (firstRequest === undefined) {
          firstRequest = request;
        } else if (request !== firstRequest) {
          // Avoid exposing search filter contents in assertion diffs.
          throw new Error(
              '[perf-saved-search] Result request changed between repetitions'
          );
        }

        const results = await resultsResponse.json() as {
          content?: unknown[];
          totalElements?: number;
        } | null;
        if (!Array.isArray(results?.content) ||
            results.content.length === 0 ||
            typeof results.totalElements !== 'number' ||
            results.totalElements < 1) {
          throw new Error(
              '[perf-saved-search] Staging fixture must return at least ' +
              'one candidate, not a missing or empty result set.'
          );
        }

        await expect(page).toHaveURL(
            new RegExp(`/admin-portal/search/${savedSearchId}(?:[?#]|$)`),
            {timeout: WAIT_MS}
        );
        await expect(
            page.locator('app-define-search app-show-candidates .heading-row h2')
        ).toBeVisible({timeout: WAIT_MS});
        await expect(
            page.locator('app-define-search app-show-candidates .results-area')
                .getByText(/^\d+\s+candidates returned$/)
        ).toBeVisible({timeout: WAIT_MS});

        // Page and results are usable. Do not time capture cleanup or JSON export.
        elapsedMs = roundMs(performance.now() - start);
      } finally {
        await capture.stop();
      }

      capture.assertSuccessful([
        {method: 'GET', path: metadataPath},
        {method: 'GET', path: filtersPath},
        {method: 'POST', path: resultsPath},
      ]);
      if (elapsedMs === null) {
        throw new Error('[perf-saved-search] Duration was not measured');
      }
      run.durationMs = elapsedMs;
    }
  } finally {
    await attachJourneyResult(testInfo, 'saved-search',
        'saved-search-performance.json', runs);
  }
});
