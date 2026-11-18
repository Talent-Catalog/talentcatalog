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

import {
  isImportantJobsApi,
  locateStableJob,
  readJobsFixture,
} from '../journeys/admin/jobs-metrics';

import {median} from '../journeys/admin/candidate-search-metrics';

test('Jobs fixture requires an explicit valid ID and a stable keyword', () => {
  expect(readJobsFixture({
    PLAYWRIGHT_PERF_JOB_ID: '42',
    PLAYWRIGHT_PERF_JOB_KEYWORD: 'PERF-JOBS-42',
  })).toEqual({
    jobId: 42,
    keyword: 'PERF-JOBS-42'
  });

  expect(() => readJobsFixture({
    PLAYWRIGHT_PERF_JOB_ID: '0',
    PLAYWRIGHT_PERF_JOB_KEYWORD: 'PERF-JOBS-42',
  })).toThrow('positive integer');

  expect(() => readJobsFixture({
    PLAYWRIGHT_PERF_JOB_ID: '42',
    PLAYWRIGHT_PERF_JOB_KEYWORD: 'x',
  })).toThrow('at least 3');

  expect(() => readJobsFixture({
    PLAYWRIGHT_PERF_JOB_ID: '1.5',
    PLAYWRIGHT_PERF_JOB_KEYWORD: 'PERF-JOBS-42',
  })).toThrow('positive integer');
});

test('Jobs fixture must resolve uniquely to the expected job ID', () => {
  expect(locateStableJob({
    content: [
      {
        id: 42,
        name: 'PERF-JOBS-42 Test Engineer'
      }
    ],
    totalElements: 1,
  }, 42)).toEqual({
    index: 0,
    name: 'PERF-JOBS-42 Test Engineer'
  });

  expect(() => locateStableJob({
    content: []
  }, 42)).toThrow('exactly one');

  expect(() => locateStableJob({
    content: [
      {
        id: 42,
        name: 'fixture'
      },
      {
        id: 43,
        name: 'another'
      }
    ],
    totalElements: 2,
  }, 42)).toThrow('exactly one');

  expect(() => locateStableJob({
    content: [
      {
        id: 42,
        name: 'fixture'
      }
    ],
    totalElements: 5,
  }, 42)).toThrow('exactly one');

  expect(() => locateStableJob({
    content: [
      {
        id: 7,
        name: 'other fixture'
      }
    ],
    totalElements: 1,
  }, 42)).toThrow('does not match');
});

test('Jobs API selection captures Admin Portal endpoints', () => {
  expect(
      isImportantJobsApi('/api/admin/job/search-paged')
  ).toBe(true);

  expect(
      isImportantJobsApi('/api/admin/job/check-unread-chats')
  ).toBe(true);

  expect(
      isImportantJobsApi('/api/admin/job/42')
  ).toBe(true);

  expect(
      isImportantJobsApi('/api/admin/job/42/job-matching-info')
  ).toBe(true);

  // Do not match unrelated endpoints.
  expect(
      isImportantJobsApi('/api/admin/candidate/search')
  ).toBe(false);

  // Reject the incorrect path used by the original implementation.
  expect(
      isImportantJobsApi('/api/job/search-paged')
  ).toBe(false);
});
test('Jobs medians follow the Candidate Search measurement rule', () => {
  expect(median([310, 140, 220])).toBe(220);
});