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

import {type Page, type Request, type Response, type TestInfo} from '@playwright/test';
import {writeFile} from 'node:fs/promises';
import {median} from './candidate-search-metrics';

export const REPEATS = 3;
export const WAIT_MS = 45_000;

export type ApiTiming = {
  method: string;
  path: string;
  status: number;
  durationMs: number;
};

export type HttpFailure = {
  method: string;
  path: string;
  status?: number;
  error?: string;
};

export type IncompleteApiTiming = {
  method: string;
  path: string;
  reason: 'still-pending' | 'timing-unavailable';
};

export type MeasuredRun = {
  repetition: number;
  durationMs: number | null;
  apiTimings: ApiTiming[];
  httpFailures: HttpFailure[];
  incompleteApiTimings: IncompleteApiTiming[];
};

export type RequiredApi = {method: string; path: string};

/** Start the timeout when the current stage begins, not at page navigation. */
export async function waitForStage<T>(
    promise: Promise<T>, timeoutMs: number, description: string
): Promise<T> {
  let timer: ReturnType<typeof setTimeout> | undefined;
  try {
    return await Promise.race([
      promise,
      new Promise<never>((_, reject) => {
        timer = setTimeout(() => reject(new Error(
            `${description} (timed out after ${timeoutMs}ms)`
        )), timeoutMs);
      }),
    ]);
  } finally {
    if (timer !== undefined) {
      clearTimeout(timer);
    }
  }
}

export function pathOf(url: string): string {
  return new URL(url).pathname;
}

/**
 * Do not export candidate numbers, saved search IDs, or URL query parameters.
 * Metrics are about API routes and timing, not the identity of a staging fixture.
 */
export function redactApiPath(path: string): string {
  return path
      .replace(/(\/api\/admin\/candidate\/number\/)[^/]+/g, '$1:candidateNumber')
      .replace(/(\/api\/admin\/candidate\/)(?!number\/)[0-9]+(?=\/|$)/g, '$1:id')
      .replace(/(\/api\/admin\/saved-search(?:-candidate)?\/)[0-9]+(?=\/|$)/g, '$1:id')
      .replace(/(\/api\/admin\/candidate-saved-list\/)[0-9]+(?=\/|$)/g, '$1:id');
}

export function roundMs(value: number): number {
  return Math.round(value * 10) / 10;
}

export function newRun(repetition: number): MeasuredRun {
  return {
    repetition,
    durationMs: null,
    apiTimings: [],
    httpFailures: [],
    incompleteApiTimings: [],
  };
}

/** Wait for the response *body* before declaring the page/API ready. */
export async function ensureOk(response: Response): Promise<void> {
  let timer: ReturnType<typeof setTimeout> | undefined;

  try {
    const failure = await Promise.race([
      response.finished(),
      new Promise<never>((_, reject) => {
        timer = setTimeout(() => reject(new Error(
            `Timed out after ${WAIT_MS}ms waiting for ` +
            `${response.request().method()} ${redactApiPath(pathOf(response.url()))}`
        )), WAIT_MS);
      }),
    ]);

    if (failure) {
      throw new Error(`Response body failed: ${failure.message}`);
    }

    if (!response.ok()) {
      throw new Error(
          `${response.request().method()} ${redactApiPath(pathOf(response.url()))} ` +
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
 * Shared HTTP timing collector for Ticket 9's two new journeys.
 * Capture requests only if they START inside the measured interval.
 * Do not include response-processing cleanup in the measured duration.
 */
export class JourneyApiCapture {
  private readonly trackedRequests = new Set<Request>();
  private readonly pendingImportant = new Map<Request, Response | null>();
  private readonly completedImportant = new Set<string>();

  constructor(
      private readonly page: Page,
      private readonly run: MeasuredRun,
      private readonly isImportant: (path: string) => boolean
  ) {}

  private readonly onRequest = (request: Request): void => {
    const path = pathOf(request.url());
    if (!path.includes('/api/')) {
      return;
    }
    this.trackedRequests.add(request);
    if (this.isImportant(path)) {
      this.pendingImportant.set(request, null);
    }
  };

  private readonly onResponse = (response: Response): void => {
    const request = response.request();
    if (!this.trackedRequests.has(request)) {
      return;
    }

    if (response.status() >= 400) {
      this.run.httpFailures.push({
        method: request.method(),
        path: redactApiPath(pathOf(response.url())),
        status: response.status(),
      });
    }

    if (this.pendingImportant.has(request)) {
      this.pendingImportant.set(request, response);
    }
  };

  private recordCompleted(request: Request, response: Response | null): void {
    const path = pathOf(request.url());
    const duration = request.timing().responseEnd;

    if (response && duration >= 0) {
      this.run.apiTimings.push({
        method: request.method(),
        path: redactApiPath(path),
        status: response.status(),
        durationMs: roundMs(duration),
      });
      if (response.ok()) {
        this.completedImportant.add(`${request.method()} ${path}`);
      }
    } else {
      this.run.incompleteApiTimings.push({
        method: request.method(),
        path: redactApiPath(path),
        reason: response ? 'timing-unavailable' : 'still-pending',
      });
    }
  }

  private readonly onRequestFinished = (request: Request): void => {
    if (!this.trackedRequests.delete(request) ||
        !this.pendingImportant.has(request)) {
      return;
    }

    const response = this.pendingImportant.get(request) ?? null;
    this.pendingImportant.delete(request);
    this.recordCompleted(request, response);
  };

  private readonly onRequestFailed = (request: Request): void => {
    if (!this.trackedRequests.delete(request)) {
      return;
    }

    this.pendingImportant.delete(request);
    this.run.httpFailures.push({
      method: request.method(),
      path: redactApiPath(pathOf(request.url())),
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
    // Exclude new requests from this run once the UI has become ready.
    this.page.off('request', this.onRequest);

    // The page may be ready shortly before the requestfinished event fires.
    const deadline = Date.now() + 3_000;
    while (this.pendingImportant.size > 0 && Date.now() < deadline) {
      await new Promise<void>(resolve => setTimeout(resolve, 50));
    }

    this.page.off('response', this.onResponse);
    this.page.off('requestfinished', this.onRequestFinished);
    this.page.off('requestfailed', this.onRequestFailed);

    for (const [request, response] of this.pendingImportant) {
      this.recordCompleted(request, response);
    }
    this.trackedRequests.clear();
    this.pendingImportant.clear();
  }

  assertSuccessful(required: ReadonlyArray<RequiredApi>): void {
    // Optional/background API failures stay in JSON but do not fail a journey.
    const requiredRoutes = new Set(required.map(item =>
        `${item.method} ${redactApiPath(item.path)}`
    ));
    const isRequired = (item: {method: string; path: string}): boolean =>
        requiredRoutes.has(`${item.method} ${item.path}`);
    const criticalFailures = this.run.httpFailures.filter(isRequired);
    if (criticalFailures.length > 0) {
      throw new Error(
          'Critical performance API failures: ' +
          criticalFailures.map(failure =>
              `${failure.method} ${failure.path} ` +
              (failure.status === undefined ? failure.error : `HTTP ${failure.status}`)
          ).join('; ')
      );
    }

    const incompleteRequired = this.run.incompleteApiTimings.filter(isRequired);
    if (incompleteRequired.length > 0) {
      throw new Error(
          'Incomplete required performance API timings: ' +
          incompleteRequired.map(item =>
              `${item.method} ${item.path} (${item.reason})`
          ).join('; ')
      );
    }

    for (const request of required) {
      if (!this.completedImportant.has(`${request.method} ${request.path}`)) {
        throw new Error(
            `Missing required measured ${request.method} ` +
            redactApiPath(request.path)
        );
      }
    }
  }
}

/** Preserve partial results as a Playwright attachment when a run fails. */
export async function attachJourneyResult(
    testInfo: TestInfo,
    journey: string,
    filename: string,
    runs: MeasuredRun[]
): Promise<void> {
  const durations = runs.flatMap(run =>
      run.durationMs === null ? [] : [run.durationMs]
  );
  const medianDurationMs =
      durations.length === REPEATS ? median(durations) : null;

  const file = testInfo.outputPath(filename);
  await writeFile(file, JSON.stringify({
    journey,
    medianDurationMs,
    runs,
  }, null, 2) + '\n');
  await testInfo.attach(filename, {
    path: file,
    contentType: 'application/json',
  });
  console.log(`${journey} median: ${medianDurationMs ?? 'incomplete'} ms`);
}
