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

/** Search must be a stable non-default saved search available to the perf user. */
export function readSavedSearchFixture(
    env: Readonly<Record<string, string | undefined>>
): number {
  const rawId = env.PLAYWRIGHT_PERF_SAVED_SEARCH_ID?.trim() ?? '';
  const id = Number(rawId);
  if (!/^[1-9][0-9]*$/.test(rawId) || !Number.isSafeInteger(id)) {
    throw new Error(
        '[perf-saved-search] PLAYWRIGHT_PERF_SAVED_SEARCH_ID must be a ' +
        'positive integer identifying a stable, non-default staging Saved Search.'
    );
  }
  return id;
}

export function isSavedSearchApi(path: string): boolean {
  return path.startsWith('/api/admin/saved-search/') ||
      path.startsWith('/api/admin/saved-search-candidate/');
}
