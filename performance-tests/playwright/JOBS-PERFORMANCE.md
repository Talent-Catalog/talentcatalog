
# Jobs Performance Journey

## Overview

The Jobs journey measures:

1. Open Live Jobs
2. Filter using a stable keyword
3. Verify the expected Job
4. Open Job Details
5. Wait for the details to render

The journey runs three times and calculates the median duration.

## Required environment variables

- `PLAYWRIGHT_PERF_JOB_ID`: Numeric ID of the staging Job.
- `PLAYWRIGHT_PERF_JOB_KEYWORD`: Unique keyword matching that Job.

Use a stable, active staging Job that appears under Live Jobs.
The keyword must return exactly one result.

## Running locally

From `performance-tests/playwright`:

```powershell
$env:PLAYWRIGHT_PERF_JOB_ID = "YOUR_JOB_ID"
$env:PLAYWRIGHT_PERF_JOB_KEYWORD = "YOUR_UNIQUE_KEYWORD"

npm run perf:smoke
npm run perf:test -- --grep "Jobs performance journey"
```

## Results

The test exports `jobs-performance.json`, containing:

- Three measured repetitions
- Median journey duration
- Jobs API timings
- HTTP and network failures
- Incomplete API timings

Warm-up and preparation are excluded from measurements.

This journey is intended for post-merge staging performance validation.
