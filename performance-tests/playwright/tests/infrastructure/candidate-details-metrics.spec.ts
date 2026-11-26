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
  isCandidateDetailsApi,
  readCandidateFixture,
} from '../journeys/admin/candidate-details-metrics';
import {
  isSavedSearchApi,
  readSavedSearchFixture,
} from '../journeys/admin/saved-search-metrics';
import {
  newRun,
  redactApiPath,
  REPEATS,
  roundMs,
} from '../journeys/admin/performance-journey-utils';
import {median} from '../journeys/admin/candidate-search-metrics';

test('Candidate Details requires a stable, safe fixture number', () => {
  expect(readCandidateFixture({
    PLAYWRIGHT_PERF_CANDIDATE_NUMBER: 'TC-12345',
  })).toBe('TC-12345');
  expect(() => readCandidateFixture({})).toThrow('PLAYWRIGHT_PERF_CANDIDATE_NUMBER');
  expect(() => readCandidateFixture({
    PLAYWRIGHT_PERF_CANDIDATE_NUMBER: 'a/b',
  })).toThrow('PLAYWRIGHT_PERF_CANDIDATE_NUMBER');
  expect(() => readCandidateFixture({
    PLAYWRIGHT_PERF_CANDIDATE_NUMBER: 'a?token=secret',
  })).toThrow('PLAYWRIGHT_PERF_CANDIDATE_NUMBER');
});

test('Saved Search fixture requires a positive safe integer', () => {
  expect(readSavedSearchFixture({
    PLAYWRIGHT_PERF_SAVED_SEARCH_ID: '123',
  })).toBe(123);
  for (const id of ['', '-3', '0', '1.5', 'not-an-id',
    '9007199254740992']) {
    expect(() => readSavedSearchFixture({
      PLAYWRIGHT_PERF_SAVED_SEARCH_ID: id,
    })).toThrow('positive integer');
  }
});

test('Only relevant Candidate Details and Saved Search APIs are important', () => {
  expect(isCandidateDetailsApi('/api/admin/candidate/number/TC-12345')).toBe(true);
  expect(isCandidateDetailsApi('/api/admin/candidate/42')).toBe(true);
  expect(isCandidateDetailsApi('/api/admin/job/42')).toBe(false);
  expect(isSavedSearchApi('/api/admin/saved-search/123/load')).toBe(true);
  expect(isSavedSearchApi(
      '/api/admin/saved-search-candidate/123/search-paged'
  )).toBe(true);
  expect(isSavedSearchApi('/api/admin/candidate/search')).toBe(false);
});

test('Performance output removes fixture IDs from API paths', () => {
  expect(redactApiPath('/api/admin/candidate/number/TC-12345'))
      .toBe('/api/admin/candidate/number/:candidateNumber');
  expect(redactApiPath('/api/admin/candidate/42/intake'))
      .toBe('/api/admin/candidate/:id/intake');
  expect(redactApiPath('/api/admin/saved-search/123/load'))
      .toBe('/api/admin/saved-search/:id/load');
  expect(redactApiPath('/api/admin/saved-search-candidate/123/search-paged'))
      .toBe('/api/admin/saved-search-candidate/:id/search-paged');
});

test('Both new journeys use three repetitions and a median', () => {
  expect(REPEATS).toBe(3);
  expect(median([620, 310, 420])).toBe(420);
  expect(roundMs(123.456)).toBe(123.5);
  expect(newRun(2)).toEqual({
    repetition: 2,
    durationMs: null,
    apiTimings: [],
    httpFailures: [],
    incompleteApiTimings: [],
  });
});
