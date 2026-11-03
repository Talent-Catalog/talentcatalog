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
import {expect, type Request, type Response, test} from '@playwright/test';
import {writeFile} from 'node:fs/promises';
import {resolve} from 'node:path';
import {performance} from 'node:perf_hooks';
import {median} from './candidate-search-metrics';

const REPEATS = 3;
const SEARCH_KEYWORD = 'engineer';
const WAIT_MS = 45_000;

// Reuse the browser session created during warm-up.
test.use({
  storageState: resolve(__dirname, '../../../.auth/perf-user.json'),
});

type ApiTiming = {
  method: string;
  path: string;
  status: number;
  durationMs: number;
};

type HttpFailure = {
  method: string;
  path: string;
  status?: number;
  error?: string;
};

type MeasuredRun = {
  repetition: number;
  durationMs: number | null;
  apiTimings: ApiTiming[];
  httpFailures: HttpFailure[];
};

// Do not export query parameters or authentication tokens.
function pathOf(url: string): string {
  return new URL(url).pathname;
}

// Capture Candidate Search and its related saved-search APIs.
function isImportantSearchApi(path: string): boolean {
  return path.endsWith('/candidate/search') ||
      path.includes('/saved-search/');
}

function roundMs(value: number): number {
  return Math.round(value * 10) / 10;
}

test('Candidate Search performance journey', async ({page}, testInfo) => {
  test.setTimeout(240_000);

  const runs: MeasuredRun[] = [];

  try {
    // Run the same journey exactly three times.
    for (let repetition = 1; repetition <= REPEATS; repetition++) {
      const run: MeasuredRun = {
        repetition,
        durationMs: null,
        apiTimings: [],
        httpFailures: [],
      };

      runs.push(run);
      const importantResponses: Response[] = [];

      // Record HTTP errors and important search responses.
      const onResponse = (response: Response) => {
        const path = pathOf(response.url());

        if (!path.includes('/api/')) return;

        if (response.status() >= 400) {
          run.httpFailures.push({
            method: response.request().method(),
            path,
            status: response.status(),
          });
        }

        if (isImportantSearchApi(path)) {
          importantResponses.push(response);
        }
      };

      // Network failures may not have an HTTP status.
      const onRequestFailed = (request: Request) => {
        const path = pathOf(request.url());

        if (path.includes('/api/')) {
          run.httpFailures.push({
            method: request.method(),
            path,
            error: request.failure()?.errorText ?? 'Network error',
          });
        }
      };

      page.on('response', onResponse);
      page.on('requestfailed', onRequestFailed);

      try {
        // Start timing before navigating to Candidate Search.
        const start = performance.now();

        const navigation = await page.goto('search', {
          waitUntil: 'domcontentloaded',
          timeout: WAIT_MS,
        });

        if (!navigation?.ok()) {
          throw new Error(
              'Opening Candidate Search returned HTTP ' +
              (navigation?.status() ?? 'no response')
          );
        }

        // Wait for the actual search form.
        const searchArea = page.locator('app-define-search');
        const form = searchArea.locator('form.filter-form');

        await expect(form).toBeVisible({timeout: WAIT_MS});

        // Clear previous filters for consistent measurements.
        await searchArea.getByRole('button', {
          name: 'Clear Search',
        }).click();

        // Use the same keyword in every repetition.
        const keyword = form.locator(
            'tc-input[formcontrolname="simpleQueryString"] input'
        );

        await keyword.fill('');
        await keyword.fill(SEARCH_KEYWORD);

// Listen for the Candidate Search API response before clicking.
        const searchResponsePromise = page.waitForResponse(
            response =>
                response.request().method() === 'POST' &&
                pathOf(response.url()).endsWith('/candidate/search'),
            {timeout: WAIT_MS}
        );

        void searchResponsePromise.catch(() => {});

        // Locate TC's Search button.
        const searchButton = form
        .locator('tc-button')
        .filter({hasText: /^\s*Search\s*$/})
        .locator('button');

        await expect(searchButton).toBeVisible();
        await expect(searchButton).toBeEnabled();

        // Execute the search.
        await searchButton.click();

        // Wait until the search API responds.
        const searchResponse = await searchResponsePromise;
        const requestError = await searchResponse.finished();

        if (requestError) {
          throw new Error('Candidate Search API did not finish');
        }

        if (!searchResponse.ok()) {
          throw new Error(
              'Candidate Search returned HTTP ' + searchResponse.status()
          );
        }

        // Wait until the loading indicator disappears.
        await expect(
            page.locator('app-show-candidates > tc-loading .tc-loader')
        ).toBeHidden({timeout: WAIT_MS});

        // Confirm the search results are actually displayed.
        await expect(
            page.locator('app-show-candidates .results-area')
            .getByText(/^\d+\s+candidates returned$/)
        ).toBeVisible({timeout: WAIT_MS});

        // Stop timing after the user can see the results.
        run.durationMs = roundMs(performance.now() - start);
      } finally {
        // Remove listeners before starting the next repetition.
        page.off('response', onResponse);
        page.off('requestfailed', onRequestFailed);

        // Record completed API response timings.

        // Record timings only for API responses that have already finished.
        // Do not wait for pending requests, as they could block the test.
        for (const response of importantResponses) {
          const request = response.request();
          const duration = request.timing().responseEnd;

          // Playwright returns -1 when the response has not finished.
          if (duration < 0) {
            continue;
          }

          run.apiTimings.push({
            method: request.method(),
            path: pathOf(response.url()),
            status: response.status(),
            durationMs: roundMs(duration),
          });
        }

      }
    }
  } finally {
    // Keep measurements even when a repetition fails.
    const durations = runs.flatMap(run =>
        run.durationMs === null ? [] : [run.durationMs]
    );

    const result = {
      journey: 'candidate-search',
      keyword: SEARCH_KEYWORD,
      medianDurationMs:
          durations.length === REPEATS ? median(durations) : null,
      runs,
    };

    // Export results for later comparison tickets.
    const file = testInfo.outputPath('candidate-search.json');

    await writeFile(file, JSON.stringify(result, null, 2) + '\n');

    await testInfo.attach('candidate-search.json', {
      path: file,
      contentType: 'application/json',
    });

    console.log(
        'Candidate Search median: ' +
        (result.medianDurationMs ?? 'incomplete') + ' ms'
    );
  }
});
