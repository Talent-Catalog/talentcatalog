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
import {expect, type Page, type Request, type Response, test} from '@playwright/test';
import {writeFile} from 'node:fs/promises';
import {resolve} from 'node:path';
import {performance} from 'node:perf_hooks';
import {median} from './candidate-search-metrics';

const REPEATS = 3;
const SEARCH_KEYWORD = 'engineer';
const WAIT_MS = 45_000;

// The warm-up project creates this authenticated browser state first.
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

type IncompleteApiTiming = {
  method: string;
  path: string;
  reason: 'still-pending' | 'timing-unavailable';
};

type MeasuredRun = {
  repetition: number;
  durationMs: number | null;
  apiTimings: ApiTiming[];
  httpFailures: HttpFailure[];
  incompleteApiTimings: IncompleteApiTiming[];
};

// Keep only the path; do not expose URL query parameters in results.
function pathOf(url: string): string {
  return new URL(url).pathname;
}

// Capture the direct candidate search and the saved-search calls it uses.
function isImportantSearchApi(path: string): boolean {
  return path === '/api/admin/candidate/search' ||
      path.startsWith('/api/admin/saved-search/') ||
      /^\/api\/admin\/saved-search-candidate\/\d+\/search-paged$/.test(path);
}

function roundMs(value: number): number {
  return Math.round(value * 10) / 10;
}

/**
 * Set the default search to the same keyword before any measured run.
 * This avoids run 1 loading different saved filters from runs 2 and 3.
 * Use a dedicated staging account: Clear Search can unlink a saved job.
 */
async function prepareSearchState(page: Page): Promise<void> {
  const navigation = await page.goto('search', {
    waitUntil: 'domcontentloaded',
    timeout: WAIT_MS,
  });

  if (!navigation?.ok()) {
    throw new Error('Failed to open Candidate Search for preparation');
  }

  const searchArea = page.locator('app-define-search');
  const form = searchArea.locator('form.filter-form');
  await expect(form).toBeVisible({timeout: WAIT_MS});

  await searchArea.getByRole('button', {name: 'Clear Search'}).click();
  await form.locator(
      'tc-input[formcontrolname="simpleQueryString"] input'
  ).fill(SEARCH_KEYWORD);

  // Register before clicking so even a fast response is captured.
  const responsePromise = page.waitForResponse(
      response =>
          response.request().method() === 'POST' &&
          pathOf(response.url()) === '/api/admin/candidate/search',
      {timeout: WAIT_MS}
  );
  void responsePromise.catch(() => {});

  const searchButton = form.locator('tc-button')
  .filter({hasText: /^\s*Search\s*$/})
  .locator('button');
  await expect(searchButton).toBeEnabled();
  await searchButton.click();

  const response = await responsePromise;
  const submitted = response.request().postDataJSON() as {
    simpleQueryString?: string;
  };
  expect(submitted.simpleQueryString).toBe(SEARCH_KEYWORD);

  const error = await response.finished();
  if (error || !response.ok()) {
    throw new Error(
        'Candidate Search preparation failed: HTTP ' + response.status()
    );
  }

  await expect(
      page.locator('app-show-candidates > tc-loading .tc-loader')
  ).toBeHidden({timeout: WAIT_MS});
  await expect(
      page.locator('app-show-candidates .results-area')
      .getByText(/^\d+\s+candidates returned$/)
  ).toBeVisible({timeout: WAIT_MS});
}

test('Candidate Search performance journey', async ({page}, testInfo) => {
  test.setTimeout(240_000);

  const runs: MeasuredRun[] = [];
  let firstSearchRequest: string | undefined;

  try {
    // The preparation search is intentionally excluded from measurements.
    await prepareSearchState(page);

    for (let repetition = 1; repetition <= REPEATS; repetition++) {
      const run: MeasuredRun = {
        repetition,
        durationMs: null,
        apiTimings: [],
        httpFailures: [],
        incompleteApiTimings: [],
      };
      runs.push(run);

      const apiRequests = new Set<Request>();
      const pendingRequests = new Set<Request>();
      const responsesByRequest = new Map<Request, Response>();
      let measuredDurationMs: number | null = null;

      // Track important requests from the moment they start.
      const onRequest = (request: Request) => {
        const path = pathOf(request.url());
        if (!path.includes('/api/')) return;

        // Ignore responses from work that began before this run.
        apiRequests.add(request);
        if (isImportantSearchApi(path)) {
          pendingRequests.add(request);
        }
      };

      const onResponse = (response: Response) => {
        const path = pathOf(response.url());
        if (!apiRequests.has(response.request())) return;

        if (response.status() >= 400) {
          run.httpFailures.push({
            method: response.request().method(),
            path,
            status: response.status(),
          });
        }

        if (isImportantSearchApi(path)) {
          responsesByRequest.set(response.request(), response);
        }
      };

      // Fired after the complete response body has been received.
      const onRequestFinished = (request: Request) => {
        if (!pendingRequests.delete(request)) return;

        const response = responsesByRequest.get(request);
        responsesByRequest.delete(request);
        const duration = request.timing().responseEnd;

        if (!response || duration < 0) {
          run.incompleteApiTimings.push({
            method: request.method(),
            path: pathOf(request.url()),
            reason: 'timing-unavailable',
          });
          return;
        }

        run.apiTimings.push({
          method: request.method(),
          path: pathOf(request.url()),
          status: response.status(),
          durationMs: roundMs(duration),
        });
      };

      const onRequestFailed = (request: Request) => {
        if (!apiRequests.has(request)) return;
        const path = pathOf(request.url());
        pendingRequests.delete(request);
        responsesByRequest.delete(request);

        if (path.includes('/api/')) {
          run.httpFailures.push({
            method: request.method(),
            path,
            error: request.failure()?.errorText ?? 'Network error',
          });
        }
      };

      page.on('request', onRequest);
      page.on('response', onResponse);
      page.on('requestfinished', onRequestFinished);
      page.on('requestfailed', onRequestFailed);

      try {
        // Timing includes navigation, applying filters and showing results.
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

        const searchArea = page.locator('app-define-search');
        const form = searchArea.locator('form.filter-form');
        await expect(form).toBeVisible({timeout: WAIT_MS});

        // The account's saved search has already been normalized.
        await searchArea.getByRole('button', {name: 'Clear Search'}).click();
        await form.locator(
            'tc-input[formcontrolname="simpleQueryString"] input'
        ).fill(SEARCH_KEYWORD);

        const searchResponsePromise = page.waitForResponse(
            response =>
                response.request().method() === 'POST' &&
                pathOf(response.url()) === '/api/admin/candidate/search',
            {timeout: WAIT_MS}
        );
        void searchResponsePromise.catch(() => {});

        const searchButton = form.locator('tc-button')
        .filter({hasText: /^\s*Search\s*$/})
        .locator('button');
        await expect(searchButton).toBeVisible();
        await expect(searchButton).toBeEnabled();
        await searchButton.click();

        const searchResponse = await searchResponsePromise;
        const submittedRequest = searchResponse.request().postDataJSON() as {
          simpleQueryString?: string;
          savedSearchId?: number;
        };

        expect(
            submittedRequest.simpleQueryString,
            'Candidate Search must submit the expected keyword'
        ).toBe(SEARCH_KEYWORD);
        expect(
            submittedRequest.savedSearchId,
            'Candidate Search must include a saved search ID'
        ).toBeTruthy();

        // Identical submissions across the three measured runs.
        const currentRequest = JSON.stringify(submittedRequest);
        if (firstSearchRequest === undefined) {
          firstSearchRequest = currentRequest;
        } else {
          expect(
              currentRequest,
              'All three repetitions must use identical search parameters'
          ).toBe(firstSearchRequest);
        }

        const requestError = await searchResponse.finished();
        if (requestError) {
          throw new Error('Candidate Search API did not finish');
        }
        if (!searchResponse.ok()) {
          throw new Error(
              'Candidate Search returned HTTP ' + searchResponse.status()
          );
        }

        await expect(
            page.locator('app-show-candidates > tc-loading .tc-loader')
        ).toBeHidden({timeout: WAIT_MS});
        await expect(
            page.locator('app-show-candidates .results-area')
            .getByText(/^\d+\s+candidates returned$/)
        ).toBeVisible({timeout: WAIT_MS});

        // Stop timing when results are visible, not when reporting completes.
        measuredDurationMs = roundMs(performance.now() - start);
      } finally {
        page.off('request', onRequest);
        page.off('response', onResponse);
        page.off('requestfinished', onRequestFinished);
        page.off('requestfailed', onRequestFailed);

        // A slow relevant API is reported, never silently skipped.
        for (const request of pendingRequests) {
          run.incompleteApiTimings.push({
            method: request.method(),
            path: pathOf(request.url()),
            reason: 'still-pending',
          });
        }
      }

      // Reject a measurement with failed or missing required API activity.
      const requiredFailures = run.httpFailures.filter(
          failure => isImportantSearchApi(failure.path)
      );
      if (requiredFailures.length > 0) {
        const details = requiredFailures.map(failure => {
          const reason = failure.status !== undefined
              ? `HTTP ${failure.status}`
              : failure.error ?? 'Network error';
          return `${failure.method} ${failure.path}: ${reason}`;
        });
        throw new Error('Candidate Search required API failure: ' + details.join('; '));
      }

      if (run.incompleteApiTimings.length > 0) {
        const details = run.incompleteApiTimings.map(item =>
            `${item.method} ${item.path} (${item.reason})`
        );
        throw new Error('Candidate Search API timings incomplete: ' + details.join('; '));
      }

      if (measuredDurationMs === null) {
        throw new Error('Candidate Search duration was not measured');
      }
      run.durationMs = measuredDurationMs;
    }
  } finally {
    // Export any completed or partial results, even when a run fails.
    const durations = runs.flatMap(run =>
        run.durationMs === null ? [] : [run.durationMs]
    );
    const result = {
      journey: 'candidate-search',
      keyword: SEARCH_KEYWORD,
      medianDurationMs: durations.length === REPEATS ? median(durations) : null,
      runs,
    };

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
