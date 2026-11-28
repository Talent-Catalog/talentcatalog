#!/usr/bin/env python3
"""Apply PR #4012 Playwright fixes safely, even after some local edits.

Run from the talentcatalog repository root or performance-tests/playwright.
Checks every code anchor and writes only after all validation succeeds.
"""
from pathlib import Path
import re
import sys

REL = Path('performance-tests/playwright')
SEARCH = REL / 'tests/journeys/admin/saved-search.spec.ts'
UTILS = REL / 'tests/journeys/admin/performance-journey-utils.ts'
JOBS = REL / 'tests/journeys/admin/jobs.spec.ts'
TESTS = REL / 'tests/infrastructure/candidate-details-metrics.spec.ts'


def find_root():
    for p in [Path.cwd(), *Path.cwd().parents]:
        if (p / SEARCH).is_file():
            return p
    raise RuntimeError('Run inside your Talent Catalog checkout (files not found).')


def replace_exact(source, old, new, label):
    # The old anchor may be contained inside the new code (for example, imports
    # and declarations). Detect an already-applied edit BEFORE looking for old.
    if new and new in source:
        print(f'Already applied: {label}')
        return source
    count = source.count(old)
    if count == 1:
        return source.replace(old, new, 1)
    raise RuntimeError(
        f'{label}: expected one original block or its replacement, '
        f'found {count} original blocks. No files changed. '
        'Your local code may have changed; inspect this section manually.'
    )


def drop_section(source, start, end, label):
    if source.count(end) != 1:
        raise RuntimeError(f'{label}: repeated/missing end marker. No files changed.')
    if start not in source:
        print(f'Already removed or not present: {label}')
        return source
    if source.count(start) != 1:
        raise RuntimeError(f'{label}: repeated start marker. No files changed.')
    i = source.index(start)
    j = source.index(end)
    if i >= j:
        raise RuntimeError(f'{label}: invalid marker order. No files changed.')
    return source[:i] + source[j:]


def fix_jobs(s):
    # Local revisions may already have removed or re-indented this diagnostic.
    # Only remove the specific debug statement; never require it to exist.
    pattern = re.compile(
        r"(?m)^[ \t]*console\.log\(\s*['\"]\[perf-jobs\] Clicking:['\"]\s*,\s*"
        r"await jobLink\.getAttribute\(['\"]href['\"]\)\s*\);[ \t]*\n?"
    )
    updated, count = pattern.subn('', s)
    if count > 1:
        raise RuntimeError('Found multiple Jobs click logs. No files changed.')
    if count == 0:
        if '[perf-jobs] Clicking:' in s:
            print('WARNING: Jobs click diagnostic has a different format; '
                  'remove it manually after reviewing the diff.')
        else:
            print('Already removed: Jobs click debug log')
    return updated


def fix_utils(s):
    s = replace_exact(s,
        'export type RequiredApi = {method: string; path: string};\n',
        '''export type RequiredApi = {method: string; path: string};

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
''', 'stage deadline helper')
    s = replace_exact(s, '''  assertSuccessful(required: ReadonlyArray<RequiredApi>): void {
    const criticalFailures = this.run.httpFailures.filter(
        failure => this.isImportant(failure.path)
    );
''', '''  assertSuccessful(required: ReadonlyArray<RequiredApi>): void {
    // Optional/background API failures stay in JSON but do not fail a journey.
    const requiredRoutes = new Set(required.map(item =>
        `${item.method} ${redactApiPath(item.path)}`
    ));
    const isRequired = (item: {method: string; path: string}): boolean =>
        requiredRoutes.has(`${item.method} ${item.path}`);
    const criticalFailures = this.run.httpFailures.filter(isRequired);
''', 'required API failure filter')
    s = replace_exact(s, '''    if (this.run.incompleteApiTimings.length > 0) {
      throw new Error(
          'Incomplete performance API timings: ' +
          this.run.incompleteApiTimings.map(item =>
''', '''    const incompleteRequired = this.run.incompleteApiTimings.filter(isRequired);
    if (incompleteRequired.length > 0) {
      throw new Error(
          'Incomplete required performance API timings: ' +
          incompleteRequired.map(item =>
''', 'required API incomplete filter')
    return s


def fix_saved_search(s):
    s = replace_exact(s,
        "import {expect, test} from '@playwright/test';\n",
        "import {expect, test, type Response} from '@playwright/test';\n", 'Saved Search response type import')
    s = drop_section(s,
        '    // Temporary diagnostics for intermittent Saved Search failures.\n',
        '    for (let repetition = 1; repetition <= REPEATS; repetition++) {\n',
        'temporary saved search console diagnostics')
    s = replace_exact(s, '''  roundMs,
  WAIT_MS,
''', '''  roundMs,
  WAIT_MS,
  waitForStage,
''', 'stage helper import')
    s = replace_exact(s, '''  let firstFilters: string | undefined;
''', '''  let firstFilters: string | undefined;
  let firstTotalElements: number | undefined;
''', 'stable candidate count')
    s = replace_exact(s, '''      // Register all response listeners *before* triggering navigation.
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
''', '''      // Register listeners before navigation so early responses cannot be missed.
      // Disable the early filter/results clocks; enforce staged deadlines below.
      const metadataPromise = page.waitForResponse(response =>
          response.request().method() === 'GET' &&
          pathOf(response.url()) === metadataPath,
      {timeout: WAIT_MS});
      const filtersStartedPromise = page.waitForRequest(request =>
          request.method() === 'GET' &&
          pathOf(request.url()) === filtersPath,
      {timeout: 0});
      const filtersPromise = page.waitForResponse(response =>
          response.request().method() === 'GET' &&
          pathOf(response.url()) === filtersPath,
      {timeout: 0});
      const resultsStartedPromise = page.waitForRequest(request =>
          request.method() === 'POST' &&
          pathOf(request.url()) === resultsPath,
      {timeout: 0});
      const resultsPromise = page.waitForResponse(response =>
          response.request().method() === 'POST' &&
          pathOf(response.url()) === resultsPath,
      {timeout: 0});
      void metadataPromise.catch(() => {});
      void filtersStartedPromise.catch(() => {});
      void filtersPromise.catch(() => {});
      void resultsStartedPromise.catch(() => {});
      void resultsPromise.catch(() => {});
''', 'Saved Search response listener setup')
    s = replace_exact(s, '''        const filtersResponse = await filtersPromise;
''', '''        // Angular finishes reference-data initialization before loading filters.
        // Give that initialization its own deadline, then time the API response.
        await waitForStage(
            filtersStartedPromise, WAIT_MS * 2,
            '[perf-saved-search] Filters GET never started; inspect reference-data APIs'
        );
        const filtersResponse = await waitForStage<Response>(
            filtersPromise, WAIT_MS,
            '[perf-saved-search] Filters GET started but had no response'
        );
''', 'Saved Search filters staged waits')
    s = replace_exact(s, '''        const resultsResponse = await resultsPromise;
''', '''        await waitForStage(
            resultsStartedPromise, WAIT_MS * 2,
            '[perf-saved-search] Results POST never started after filters loaded'
        );
        const resultsResponse = await waitForStage<Response>(
            resultsPromise, WAIT_MS,
            '[perf-saved-search] Results POST started but had no response'
        );
''', 'Saved Search results staged waits')
    s = replace_exact(s, '''        await expect(page).toHaveURL(
            new RegExp(`/admin-portal/search/${savedSearchId}(?:[?#]|$)`),
''', '''        // A stable staging fixture must return a comparable result size.
        if (firstTotalElements === undefined) {
          firstTotalElements = results.totalElements;
        } else if (results.totalElements !== firstTotalElements) {
          throw new Error(
              '[perf-saved-search] Candidate result count changed between repetitions'
          );
        }

        await expect(page).toHaveURL(
            new RegExp(`/admin-portal/search/${savedSearchId}(?:[?#]|$)`),
''', 'stable results insertion')
    s = replace_exact(s, '''            page.locator('app-define-search app-show-candidates .results-area')
                .getByText(/^\\d+\\s+candidates returned$/)
''', '''            page.locator('app-define-search app-show-candidates .results-area')
                .getByText(`${results.totalElements} candidates returned`, {exact: true})
''', 'UI and API count comparison')
    return s


def fix_tests(s):
    if "test('Only required API failures and incomplete requests are blocking'" in s:
        print('Already applied: infrastructure regression tests')
        return s
    s = replace_exact(s,
        "import {expect, test} from '@playwright/test';\n",
        "import {expect, test, type Page} from '@playwright/test';\n", 'test Page import')
    s = replace_exact(s,
        '''  newRun,
  redactApiPath,
  REPEATS,
  roundMs,
''',
        '''  JourneyApiCapture,
  newRun,
  redactApiPath,
  REPEATS,
  roundMs,
  waitForStage,
''', 'additional unit test imports')
    s += '''
// Optional requests remain visible in output without invalidating the journey.
test('Only required API failures and incomplete requests are blocking', () => {
  const run = newRun(1);
  run.httpFailures.push({
    method: 'GET', path: '/api/admin/saved-search/:id/background', status: 503,
  });
  run.incompleteApiTimings.push({
    method: 'GET', path: '/api/admin/saved-search/:id/background',
    reason: 'still-pending',
  });
  const capture = new JourneyApiCapture({} as Page, run, () => true);
  expect(() => capture.assertSuccessful([])).not.toThrow();
  expect(() => capture.assertSuccessful([
    {method: 'GET', path: '/api/admin/saved-search/123/background'},
  ])).toThrow('Critical performance API failures');

  run.httpFailures.length = 0;
  expect(() => capture.assertSuccessful([
    {method: 'GET', path: '/api/admin/saved-search/123/background'},
  ])).toThrow('Incomplete required performance API timings');
});

test('Saved Search stage deadlines identify stalled APIs', async () => {
  await expect(waitForStage(Promise.resolve('ok'), 100, 'ready'))
      .resolves.toBe('ok');
  await expect(waitForStage(
      new Promise<never>(() => {}), 10, 'Filters GET not started'
  )).rejects.toThrow('Filters GET not started');
});
'''
    return s


def main():
    root = find_root()
    plans = [
        (JOBS, fix_jobs),
        (SEARCH, fix_saved_search),
        (UTILS, fix_utils),
        (TESTS, fix_tests),
    ]
    edits = []
    for path, transform in plans:
        absolute = root / path
        original = absolute.read_text(encoding='utf-8')
        updated = transform(original)
        if updated != original:
            edits.append((absolute, updated))
        else:
            print('No additional edit:', path)
    # Avoid writing ANY files if a required anchor check failed above.
    for absolute, updated in edits:
        absolute.write_text(updated, encoding='utf-8')
        print('Updated', absolute.relative_to(root))
    if edits:
        print('\nApplied PR #4012 fixes. Review git diff and run perf:smoke, then journeys.')
    else:
        print('\nNo changes required by this script.')


if __name__ == '__main__':
    try:
        main()
    except (RuntimeError, OSError) as error:
        print(f'ERROR: {error}', file=sys.stderr)
        sys.exit(1)
