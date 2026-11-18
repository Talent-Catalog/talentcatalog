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

export type JobsFixture = {
  jobId: number;
  keyword: string;
};

/**
 * The fixture must be a dedicated, published Live Job on TBB staging.
 * Its unique keyword must return exactly one result (the configured job ID).
 * Never silently fall back to whichever job happens to be first.
 */
export function readJobsFixture(
    env: Readonly<Record<string, string | undefined>>
): JobsFixture {
  const rawId = env.PLAYWRIGHT_PERF_JOB_ID?.trim() ?? '';
  const keyword = env.PLAYWRIGHT_PERF_JOB_KEYWORD?.trim() ?? '';
  const jobId = Number(rawId);

  if (!/^[1-9]\d*$/.test(rawId) || !Number.isSafeInteger(jobId)) {
    throw new Error(
        '[perf-jobs] PLAYWRIGHT_PERF_JOB_ID must be a positive integer ' +
        'identifying a stable, published staging Live Job.'
    );
  }

  if (keyword.length < 3) {
    throw new Error(
        '[perf-jobs] PLAYWRIGHT_PERF_JOB_KEYWORD must be at least 3 ' +
        'characters and uniquely match the staging fixture.'
    );
  }

  return {jobId, keyword};
}

/**
 * Reject changing fixtures or non-unique results
 * instead of measuring random jobs.
 */
export function locateStableJob(
    responseBody: unknown,
    expectedJobId: number
): {index: number; name: string} {
  const body = responseBody as {
    content?: Array<{id?: number; name?: string}>;
    totalElements?: number;
  } | null;

  if (!body || !Array.isArray(body.content) ||
      body.content.length !== 1 ||
      (typeof body.totalElements === 'number' &&
          body.totalElements !== 1)) {
    throw new Error(
        '[perf-jobs] The fixture keyword must return exactly one Live Job. ' +
        'Check staging data and PLAYWRIGHT_PERF_JOB_KEYWORD.'
    );
  }

  const job = body.content[0];

  if (job?.id !== expectedJobId ||
      typeof job.name !== 'string' ||
      !job.name.trim()) {
    throw new Error(
        '[perf-jobs] The filtered Job does not match PLAYWRIGHT_PERF_JOB_ID. ' +
        'Check that the fixture remains published and accessible.'
    );
  }

  return {
    index: 0,
    name: job.name
  };
}

/**
 * Capture the same Jobs service requests seen by
 * the Admin Portal UI.
 */
export function isImportantJobsApi(path: string): boolean {
  return path.startsWith('/api/admin/job/');
}