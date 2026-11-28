# Candidate Details and Saved Search Performance Journeys

These two Playwright journeys extend the **post-merge TBB staging** performance
suite. They **do not create a pre-merge performance gate**.

## What is measured

- **Candidate Details:** Navigate to a known candidate's Admin Portal profile,
  fetch the exact candidate via `GET /api/admin/candidate/number/{number}`,
  and wait for the candidate header and profile tabs to be visible.
- **Saved Search:** Navigate to an existing saved search, load its definition,
  and wait for `POST /api/admin/saved-search-candidate/{id}/search-paged`
  and the candidate results to render. Require at least one result.

Both journeys reuse the authenticated warm-up state, Chromium, and one worker.
Each journey runs **three measured repetitions** and calculates the **median**.
Preparation/reset and JSON/reporting are excluded from timed duration.
The code captures relevant API timings, HTTP failures, incomplete requests,
and keeps partial data if a repetition fails.

## Required TBB staging fixtures

Configure these as CI environment variables or local environment variables:

- `PLAYWRIGHT_PERF_CANDIDATE_NUMBER`: Candidate number for a dedicated,
  accessible, stable **non-sensitive** staging candidate. 2-48 characters,
  letters/numbers/hyphens. Do not put a real person's identifying details in
  the test fixture or commit the fixture number to the repository.
- `PLAYWRIGHT_PERF_SAVED_SEARCH_ID`: Positive integer ID of an existing,
  **non-default**, accessible, non-sensitive staging Saved Search that returns
  at least one candidate. Do not update its filters during a measurement run.

The staging account used by warm-up must be allowed to read both fixtures.
The Saved Search request/filter payloads must be identical across repetitions.
No real candidate names, query strings, or fixture IDs are written to the JSON
artifacts; sensitive segments in relevant API paths are redacted.

## Execution

From `performance-tests/playwright` with dependencies and Chromium installed:

```bash
# Supply staging-only fixture values outside source control.
export PLAYWRIGHT_PERF_CANDIDATE_NUMBER='YOUR_STAGING_CANDIDATE_NUMBER'
export PLAYWRIGHT_PERF_SAVED_SEARCH_ID='YOUR_STAGING_SAVED_SEARCH_ID'

# Pure fixture/metric checks, does not require staging authentication.
npm run perf:smoke

# Warm-up precedes both browser journeys automatically.
npm run perf:test -- --grep 'Candidate Details performance journey|Saved Search performance journey'
```

A successful run writes `candidate-details-performance.json` and
`saved-search-performance.json` as Playwright attachments under `test-results`.
The per-journey JSON matches the existing tests' shape:

```json
{
  "journey": "candidate-details",
  "medianDurationMs": 400.0,
  "runs": [
    {
      "repetition": 1,
      "durationMs": 400.0,
      "apiTimings": [],
      "httpFailures": [],
      "incompleteApiTimings": []
    }
  ]
}
```

(The JSON above is a schema example, not a measured result.)

## Required verification after merge

1. Confirm staging deployment completion, service health, and expected SHA
   using the existing foundation workflows (Tickets 1-4).
2. Confirm the dedicated account and both fixtures are still accessible.
3. Run the fixture/metric checks and both measured journeys on staging.
4. Confirm **three completed runs** and a non-null median for each journey.
5. Check important API timings and failure arrays in attached JSON files.
6. Verify existing Candidate Search and Jobs journeys still work.

Do not add S3 persistence, baseline comparison, regression classification,
release gates, or the canonical result schema from later roadmap tickets here.
