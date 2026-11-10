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
import {
  isImportantJobsApi,
  type JobsFixture,
  locateStableJob,
  readJobsFixture,
} from './jobs-metrics';

const REPEATS = 3;
const WAIT_MS = 45_000;

const JOBS_ROUTE = 'jobs?tab=LiveJobs';
const SEARCH_PATH = '/api/admin/job/search-paged';

const FILTER_SELECTOR =
    'app-jobs form tc-input[formcontrolname="keyword"] input';
const JOBS_TABLE_SELECTOR = 'app-jobs tc-table table';
const JOB_ROWS_SELECTOR = 'app-jobs tc-table tbody tr';
const JOB_HEADER_SELECTOR =
    'app-view-job-from-url app-view-job .job-header-container h2';

// The existing warm-up dependency creates this authenticated state.
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

const pathOf = (url: string): string => new URL(url).pathname;

const roundMs = (ms: number): number =>
    Math.round(ms * 10) / 10;

const keywordInput = (page: Page) =>
    page.locator(FILTER_SELECTOR);

/**
 * Wait for the complete response body.
 */
async function ensureOk(response: Response): Promise<void> {
  let timer: ReturnType<typeof setTimeout> | undefined;

  try {
    const error = await Promise.race([
      response.finished(),
      new Promise<never>((_, reject) => {
        timer = setTimeout(() => reject(new Error(
            `Timed out waiting for ${response.request().method()} ` +
            `${pathOf(response.url())} after ${WAIT_MS}ms`
        )), WAIT_MS);
      }),
    ]);

    if (error) {
      throw new Error(`Response body failed: ${error.message}`);
    }

    if (!response.ok()) {
      throw new Error(
          `${response.request().method()} ${pathOf(response.url())} ` +
          `returned HTTP ${response.status()}`
      );
    }
  } finally {
    if (timer !== undefined) {
      clearTimeout(timer);
    }
  }
}

/**
 * Change the keyword and wait for that specific search,
 * not an unrelated background request.
 */
async function setKeyword(
    page: Page,
    keyword: string
): Promise<Response> {
  // Register the listener before changing the input.
  const responsePromise = page.waitForResponse(
      response => {
        if (
            response.request().method() !== 'POST' ||
            pathOf(response.url()) !== SEARCH_PATH
        ) {
          return false;
        }

        const payload = response.request().postDataJSON() as {
          keyword?: string | null;
        };

        return (payload.keyword ?? '') === keyword;
      },
      {timeout: WAIT_MS}
  );

  void responsePromise.catch(() => {});

  await keywordInput(page).fill(keyword);

  const response = await responsePromise;

  await ensureOk(response);

  return response;
}
/**
 * Open Live Jobs.
 *
 * Navigation alone is not guaranteed to trigger
 * a new search API request.
 */
async function openLiveJobs(page: Page): Promise<void> {
  const navigation = await page.goto(JOBS_ROUTE, {
    waitUntil: 'domcontentloaded',
    timeout: WAIT_MS,
  });

  if (!navigation?.ok()) {
    throw new Error(
        '[perf-jobs] Opening Jobs returned HTTP ' +
        (navigation?.status() ?? 'no response')
    );
  }

  const liveTab = page
  .locator('app-job-home tc-tabs ul.nav-tabs a.nav-link')
  .filter({hasText: /Live Jobs/});

  await expect(
      liveTab,
      '[perf-jobs] Live Jobs tab is unavailable to the performance account'
  ).toHaveCount(1, {timeout: WAIT_MS});

  const classes = await liveTab.getAttribute('class');

  if (!classes?.split(/\s+/).includes('active')) {
    await liveTab.click();
  }

  await expect(liveTab).toHaveClass(
      /(?:^|\s)active(?:\s|$)/,
      {timeout: WAIT_MS}
  );

  await expect(keywordInput(page)).toBeVisible({
    timeout: WAIT_MS,
  });

  await expect(
      page.locator(JOBS_TABLE_SELECTOR)
  ).toBeVisible({
    timeout: WAIT_MS,
  });

  await expect(
      page.locator('app-jobs > tc-loading .tc-loader')
  ).toBeHidden({
    timeout: WAIT_MS,
  });
}

/**
 * Reset the Jobs state between runs.
 *
 * Preparation is excluded from the measured duration.
 */
async function prepareUnfilteredJobs(page: Page): Promise<void> {
  await openLiveJobs(page);

  const input = keywordInput(page);

  if (await input.inputValue()) {
    await setKeyword(page, '');
  }

  await expect(input).toHaveValue('');
}

/**
 * Capture Jobs API timings and all API HTTP/network failures.
 */
class JobsApiCapture {
  private readonly apiRequests = new Set<Request>();

  private readonly pendingJobs =
      new Map<Request, Response | null>();

  constructor(
      private readonly page: Page,
      private readonly run: MeasuredRun
  ) {}

  private readonly onRequest = (request: Request): void => {
    const path = pathOf(request.url());

    if (!path.includes('/api/')) {
      return;
    }

    this.apiRequests.add(request);

    if (isImportantJobsApi(path)) {
      this.pendingJobs.set(request, null);
    }
  };

  private readonly onResponse = (response: Response): void => {
    const request = response.request();

    if (!this.apiRequests.has(request)) {
      return;
    }

    if (response.status() >= 400) {
      this.run.httpFailures.push({
        method: request.method(),
        path: pathOf(response.url()),
        status: response.status(),
      });
    }

    if (this.pendingJobs.has(request)) {
      this.pendingJobs.set(request, response);
    }
  };

  private readonly onRequestFinished = (request: Request): void => {
    if (
        !this.apiRequests.delete(request) ||
        !this.pendingJobs.has(request)
    ) {
      return;
    }

    const response = this.pendingJobs.get(request);

    this.pendingJobs.delete(request);

    const duration = request.timing().responseEnd;

    if (response && duration >= 0) {
      this.run.apiTimings.push({
        method: request.method(),
        path: pathOf(request.url()),
        status: response.status(),
        durationMs: roundMs(duration),
      });
    } else {
      this.run.incompleteApiTimings.push({
        method: request.method(),
        path: pathOf(request.url()),
        reason: 'timing-unavailable',
      });
    }
  };

  private readonly onRequestFailed = (request: Request): void => {
    if (!this.apiRequests.delete(request)) {
      return;
    }

    this.pendingJobs.delete(request);

    this.run.httpFailures.push({
      method: request.method(),
      path: pathOf(request.url()),
      error: request.failure()?.errorText ?? 'Network error',
    });
  };

  start(): void {
    this.page.on('request', this.onRequest);
    this.page.on('response', this.onResponse);
    this.page.on('requestfinished', this.onRequestFinished);
    this.page.on('requestfailed', this.onRequestFailed);
  }

  async stop(): Promise<void> {
    // Do not track requests created after the timer stops.
    this.page.off('request', this.onRequest);

    // Allow already-started requests to complete.
    // This wait is excluded from the measured duration.
    const deadline = Date.now() + 3_000;

    while (
        this.pendingJobs.size > 0 &&
        Date.now() < deadline
        ) {
      await new Promise<void>(
          resolve => setTimeout(resolve, 50)
      );
    }

    this.page.off('response', this.onResponse);
    this.page.off('requestfinished', this.onRequestFinished);
    this.page.off('requestfailed', this.onRequestFailed);

    for (const request of this.pendingJobs.keys()) {
      this.run.incompleteApiTimings.push({
        method: request.method(),
        path: pathOf(request.url()),
        reason: 'still-pending',
      });
    }

    this.apiRequests.clear();
    this.pendingJobs.clear();
  }
}

/**
 * Reject runs with failed or incomplete Jobs API requests.
 */
function validateRun(
    run: MeasuredRun,
    jobId: number
): void {
  const failures = run.httpFailures.filter(
      failure => isImportantJobsApi(failure.path)
  );

  if (failures.length > 0) {
    throw new Error(
        '[perf-jobs] Jobs API failure: ' +
        failures.map(failure =>
            `${failure.method} ${failure.path}: ` +
            (
                failure.status === undefined
                    ? failure.error
                    : `HTTP ${failure.status}`
            )
        ).join('; ')
    );
  }

  if (run.incompleteApiTimings.length > 0) {
    throw new Error(
        '[perf-jobs] Jobs API timings incomplete: ' +
        run.incompleteApiTimings.map(item =>
            `${item.method} ${item.path} (${item.reason})`
        ).join('; ')
    );
  }

  const requiredRequests = [
    ['POST', SEARCH_PATH],
    ['GET', `/api/admin/job/${jobId}`],
  ];

  for (const [method, path] of requiredRequests) {
    const captured = run.apiTimings.some(
        item =>
            item.method === method &&
            item.path === path
    );

    if (!captured) {
      throw new Error(
          `[perf-jobs] Missing measured ${method} ${path} API timing`
      );
    }
  }
}

/**
 * Measure a complete Jobs journey:
 * Jobs -> Filter -> Open Job Details.
 */
async function measureJobsJourney(
    page: Page,
    fixture: JobsFixture,
    referenceRequest: {value?: string},
    run: MeasuredRun
): Promise<void> {
  const capture = new JobsApiCapture(page, run);
  let elapsedMs: number | null = null;

  capture.start();

  try {
    const start = performance.now();

    // 1. Open Live Jobs.
    await openLiveJobs(page);

    await expect(keywordInput(page)).toHaveValue('');

    // 2. Apply the stable keyword filter.
    const filtered = await setKeyword(page, fixture.keyword);

    const payload = filtered.request().postDataJSON() as {
      keyword?: string;
    };

    expect(payload.keyword).toBe(fixture.keyword);

    // Use identical search parameters in all repetitions.
    const serialized = JSON.stringify(payload);

    if (referenceRequest.value === undefined) {
      referenceRequest.value = serialized;
    } else {
      expect(
          serialized,
          'Jobs filter payload changed between repetitions'
      ).toBe(referenceRequest.value);
    }

    // 3. Verify the expected Job.
    const selected = locateStableJob(
        await filtered.json(),
        fixture.jobId
    );

    const rows = page.locator(JOB_ROWS_SELECTOR);

    await expect(rows).toHaveCount(1, {
      timeout: WAIT_MS,
    });

    const row = rows.nth(selected.index);

    await expect(
        row.locator('td').first()
    ).toContainText(
        selected.name.slice(0, 30),
        {timeout: WAIT_MS}
    );

    // The first matching Job is automatically
    // shown in the details side panel.
    await expect(
        page.locator(
            'app-jobs-with-detail app-view-job ' +
            '.job-header-container h2'
        )
    ).toHaveText(selected.name, {
      timeout: WAIT_MS,
    });

    // 4. Open the full Job Details page.
    const jobLink = row.locator(
        'tc-icon[type="link"] a'
    ).first();

    await expect(jobLink).toHaveAttribute(
        'href',
        new RegExp(`/job/${fixture.jobId}$`)
    );

    const detailPath = `/api/admin/job/${fixture.jobId}`;

// Register the API listener before clicking.
    const detailResponsePromise = page.waitForResponse(
        response =>
            response.request().method() === 'GET' &&
            pathOf(response.url()) === detailPath,
        {timeout: WAIT_MS}
    );

    void detailResponsePromise.catch(() => {});

// Open Job Details.
    await jobLink.click();

// Wait for the matching API response.
    const detailResponse = await detailResponsePromise;

    await ensureOk(detailResponse);

    await expect(page).toHaveURL(
        new RegExp(
            `/admin-portal/job/${fixture.jobId}(?:[?#]|$)`
        ),
        {timeout: WAIT_MS}
    );

    // The journey ends only after details are rendered.
    await expect(
        page.locator(JOB_HEADER_SELECTOR)
    ).toHaveText(selected.name, {
      timeout: WAIT_MS,
    });

    elapsedMs = roundMs(performance.now() - start);

  } finally {
    await capture.stop();
  }

  if (elapsedMs === null) {
    throw new Error(
        '[perf-jobs] Jobs duration was not measured'
    );
  }

  validateRun(run, fixture.jobId);

  run.durationMs = elapsedMs;
}

/**
 * Three measured repetitions and a median.
 * Export partial data even if a repetition fails.
 */
test('Jobs performance journey', async ({page}, testInfo) => {
  test.setTimeout(360_000);

  const runs: MeasuredRun[] = [];
  const referenceRequest: {value?: string} = {};

  let fixture: JobsFixture | undefined;

  try {
    fixture = readJobsFixture(process.env);

    for (
        let repetition = 1;
        repetition <= REPEATS;
        repetition++
    ) {
      await prepareUnfilteredJobs(page);

      const run: MeasuredRun = {
        repetition,
        durationMs: null,
        apiTimings: [],
        httpFailures: [],
        incompleteApiTimings: [],
      };

      runs.push(run);

      await measureJobsJourney(
          page,
          fixture,
          referenceRequest,
          run
      );
    }
  } finally {
    const durations = runs.flatMap(run =>
        run.durationMs === null
            ? []
            : [run.durationMs]
    );

    const result = {
      journey: 'jobs',
      keyword: fixture?.keyword ?? null,
      jobId: fixture?.jobId ?? null,
      medianDurationMs:
          durations.length === REPEATS
              ? median(durations)
              : null,
      runs,
    };

    const file = testInfo.outputPath(
        'jobs-performance.json'
    );

    await writeFile(
        file,
        JSON.stringify(result, null, 2) + '\n'
    );

    await testInfo.attach(
        'jobs-performance.json',
        {
          path: file,
          contentType: 'application/json',
        }
    );

    console.log(
        `Jobs median: ${result.medianDurationMs ?? 'incomplete'} ms`
    );
  }
});
