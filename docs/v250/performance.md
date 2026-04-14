---
title: Release Notes 2.5.0
description: Performance Boost
sass:
  style: compressed
---

# ⚡ Performance Improvements

## 🚀 Improved candidate data loading

We’ve made a fundamental change to how candidate details are fetched, for faster search results and
list displays. In many cases, users should see candidate searches and list views load 3 to 4 times
faster.

## 💼 Faster Jobs tab

We’ve introduced performance improvements to the Jobs tab, helping pages load more quickly and
making it easier to quickly hop between jobs tabs.

## 👤 Quicker list views and candidate profiles

We’ve optimized both candidate list views and individual candidate profile pages to improve
responsiveness and reduce load times when viewing candidate information.

# ⚙ Performance Test Engineering Improvements

## 🧪 Expanded Performance Testing Coverage

<div class="card-image-container">
    <img class="card-image" 
         src="./../assets/images/v250/perf_project_structure.png" 
         alt="Performance test project structure"
    >
</div>

To support these performance improvements, we’ve significantly expanded Talent Catalog’s automated
performance testing coverage.

The performance test suite now covers both HTTP-based and DB-based performance testing, giving the
team better visibility into how core workflows behave under load and making it easier to isolate
bottlenecks when regressions occur.

Coverage now includes key workflows such as candidate search, saved-list paged search, health-check
validation, and database-focused candidate search testing.

## 🔄 Multiple Load Models for Better Comparison

<div class="card-image-container">
    <img class="card-image" 
         src="./../assets/images/v250/perf_simulations.png" 
         alt="Performance simulations"
    >
</div>

The new simulation coverage supports several different workload models so performance can be tested
in ways that better reflect real usage.

This includes:
- sequential A/B comparisons for side-by-side checks of old and new implementations
- random A/B simulations for mixed traffic patterns
- parallel closed simulations for fixed-concurrency testing
- parallel open simulations for arrival-rate based load testing

## 🌙 Automated Smoke, Nightly and Soak Runs

<div class="card-image-container">
    <img class="card-image" 
         src="./../assets/images/v250/perf_workflows.png" 
         alt="Performance workflows"
    >
</div>

We’ve also improved the automation around performance testing, with dedicated smoke, nightly and
soak workflows added to CI.

These workflows make it easier to run lightweight checks during development, schedule broader
nightly coverage, and run longer soak-style tests where needed.

## 📊 Better Reporting and Threshold Checks

Performance testing now includes improved support for summarising Gatling results and applying
threshold checks automatically.

This means test runs can now produce clearer summaries for review, while also supporting automatic
checks around failed request rates and latency thresholds.

## 💪 Stronger Foundations for Future Performance Work

Under the hood, the performance testing module has been reorganised to provide a clearer structure
for shared configuration, payloads, requests, scenarios, simulations, SQL resources and helper
scripts.

This creates a stronger foundation for future performance work by making the suite easier to extend,
easier to run consistently in CI, and easier for developers to build on as new workflows are added
to Talent Catalog.