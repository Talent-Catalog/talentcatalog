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

/** Use a dedicated non-sensitive staging fixture; never pick a random candidate. */
export function readCandidateFixture(
    env: Readonly<Record<string, string | undefined>>
): string {
  const candidateNumber = env.PLAYWRIGHT_PERF_CANDIDATE_NUMBER?.trim() ?? '';
  if (!/^[a-zA-Z0-9][a-zA-Z0-9-]{1,47}$/.test(candidateNumber)) {
    throw new Error(
        '[perf-candidate-details] PLAYWRIGHT_PERF_CANDIDATE_NUMBER must ' +
        'identify one stable, accessible staging candidate (2-48 letters, ' +
        'numbers, or hyphens).'
    );
  }
  return candidateNumber;
}

export function isCandidateDetailsApi(path: string): boolean {
  return path.startsWith('/api/admin/candidate/') ||
      path.startsWith('/api/admin/candidate-saved-list/');
}
