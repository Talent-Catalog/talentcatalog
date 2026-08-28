---
title: Release Notes 2.6.0
description: What's new in this release
permalink: v260
sass:
  style: compressed
---

# New Features

## AI Job Matching

Talent Catalog now runs a hybrid matching engine behind the scenes, combining traditional
text search with AI-generated vector embeddings. This release brings the first user-facing
pieces of that engine online — skill-aware search from a job description, and keyword
filtering alongside AI matches.

<div class="card-container">

  <a href="./v260/ai_job_matching" class="card">
    <img src="./assets/images/v260/AiJobMatching.png" alt="AI Job Matching" class="card-image">
    <div class="card-body">
      <div class="card-title">AI Job Matching</div>
      <div class="card-description">
        The first pieces of AI-powered candidate matching have landed — skill-aware search
        from a job description, and keyword filtering alongside AI matches.
      </div>
      <div class="card-footer">
        <button class="btn btn-sm">Learn more</button>
      </div>
    </div>
  </a>

</div>

## Verify+

GRN candidates who hold a UNHCR Verify+ card can now scan its QR code straight from their
phone to capture and verify their UNHCR registration number — either from the Services tab,
or as an optional step during registration.

<div class="card-container">

  <a href="./v260/verify_plus" class="card">
    <img src="./assets/images/v260/VerifyPlusServicesCard.png" alt="Verify+" class="card-image">
    <div class="card-body">
      <div class="card-title">UNHCR Verify+</div>
      <div class="card-description">
        Scan your UNHCR Verify+ QR code to capture and verify your UNHCR registration number.
      </div>
      <div class="card-footer">
        <button class="btn btn-sm">Learn more</button>
      </div>
    </div>
  </a>

</div>

## PiFi Casi Integration

GRN candidates now have a new CASI service card signposting them to PiFi Property, helping
them explore rental homes in their new country — starting with Australia.

<div class="card-container">

  <a href="./v260/pifi_signposting" class="card">
    <img src="./assets/images/v260/PiFiServicesTab.png" alt="PiFi Signposting" class="card-image">
    <div class="card-body">
      <div class="card-title">PiFi Property</div>
      <div class="card-description">
        Helping migrants find a place to call home — signposting to PiFi's property search,
        right from the Services tab.
      </div>
      <div class="card-footer">
        <button class="btn btn-sm">Learn more</button>
      </div>
    </div>
  </a>

</div>

## Other New Features

<div class="card-container">

  <a href="./v260/principal_occupation" class="card">
    <img src="./assets/images/v260/PrincipalOccupation.png" alt="Principal Occupation" class="card-image">
    <div class="card-body">
      <div class="card-title">TODO — PENDING MERGE</div>
      <div class="card-description">TODO — do not publish until TC-658 / TC-1434 merge (PR #3711)</div>
      <div class="card-footer">
        <button class="btn btn-sm">Learn more</button>
      </div>
    </div>
  </a>

</div>

# User Guides

TODO — nothing confirmed shipped this release (TC-1450/TC-1451 still in progress). Drop this
section from the published page if nothing lands before release.

# General Improvements

* TODO — Candidate deletion & erasure permissions (TC-1358, TC-1419, TC-1321, TC-1396, TC-1398)
* TODO — Submission List filtering and field visibility (TC-1113)
* TODO — Task email alert feedback and translation fix (TC-1337)

# Data Improvements

* TODO — New TC-identified skills table (TC-1058)
* TODO — Upgrade/migration note: run `build_embeddings` admin action after deploying to
  populate AI-matching embeddings for existing data (see 5 new migrations, V2_19–V2_23)

# UI / UX Enhancements

* TODO — TC-1371, TC-1335, TC-1336, TC-1339, TC-1361, TC-1407, TC-1424, TC-1412
* TODO — TC-723 (email paste validation) — confirm merged before publishing

# Performance Improvements

* TODO — Batch embedding rebuild skips records that already have one (TC-1438)
* TODO — Matching tuning and tidy up (TC-1409)

# Security Updates

TODO — nothing shipped this release (TC-1431 still Todo). Drop this section from the
published page if nothing changes before release.

# Bug Fixes

* TODO — TC-1305, TC-1340, TC-1368, TC-1446, TC-1338

# Developer Notes

* TODO — TC-1315, TC-1389

## Test Coverage

* TODO — TC-1408, TC-1382, TC-1394, TC-1404, TC-1270, TC-1272, TC-1277, TC-1279, TC-1353,
  TC-1400, TC-1425, TC-1401, TC-1403 (confirm TC-1404's tests actually pass before publishing)

## Code Refactoring

* TODO — TC-1341, TC-1372, TC-1440, TC-1323

## Continuous Integration & Deployment

* TODO — TC-1359

## Cloud Enhancements

TODO — nothing found in this release's range. Drop this section from the published page.

## New Tools and Standards

* TODO — TC-1411, TC-1342, TC-1420

---

Thank you for using Talent Catalog! Your feedback and support are invaluable to us. If you encounter
any issues or have suggestions for improvement, please don't hesitate to [contact us](mailto:support@talentcatalog.net) or
[open an issue on GitHub](https://github.com/Talent-Catalog/talentcatalog/issues).

*[Access the latest version](https://tctalent.org/admin-portal/login)*
