---
title: Release Notes 2.5.0
description: What's new in this release
permalink: v250
sass:
  style: compressed
---

# New Features

<div class="card-container">

  <a href="./v250/performance" class="card">
    <img src="./assets/images/v250/performance.png" alt="Performance improvements" class="card-image">
    <div class="card-body">
      <div class="card-title">Performance Improvements</div>
      <div class="card-description">
        Much of our work for v2.5.0 was focused on performance — delivering meaningful improvements to loading 
        times and overall responsiveness.
      </div>
      <div class="card-footer">
        <button class="btn btn-sm">Learn more</button>
      </div>
    </div>
  </a>

  <a href="./v250/linkedin_service" class="card">
    <img src="./assets/images/v250/ServicesTabLinkedIn.png" alt="LinkedIn Service" class="card-image">
    <div class="card-body">
      <div class="card-title">LinkedIn Candidate Assistance Service</div>
      <div class="card-description">
        Eligible candidates can now redeem a free 1-year Premium membership upgrade via 
        Candidate Portal self-service.
      </div>
      <div class="card-footer">
        <button class="btn btn-sm">Learn more</button>
      </div>
    </div>
  </a>

</div>

# User Guides

Helpful TC user guides:
<ul>
    <li>
        <a href="https://tc-api.redocly.app/openapi" 
        target="_blank">Talent Catalog API on Redoc</a>
    </li>
    <li>
        <a href="https://github.com/Talent-Catalog/talentcatalog/blob/staging/server/src/main/java/org/tctalent/server/casi/README.md" 
        target="_blank">CASI (Candidate Assistance Services Interface) -- Developer Guide</a>
    </li>    
    <li>
        <a href="https://drive.google.com/file/d/1CBBYNjuRrYgOQ0xDRjiegqoznek_i-MB/view?usp=drive_link" 
        target="_blank">Italy Train-to-Hire: Task Management Documentation</a>
    </li>
</ul>

## General Improvements

- Gatling version updated to enable Java-based performance testing
- Performance test folder structure standardised

## Data Improvements

- New candidate status: Ineligible (review) - supports eligibility policy revision
- New Job closed-lost stage: Inadequate pathway provision
- Automated Salesforce update of relocating dependants trigger amended
- Dependant gender and DOB intake questions marked required

# UI / UX Enhancements

-

## Other UI / UX Enhancements

- Font Awesome icon processing standardised across portals
- Removal of TC Chats pending new UX design and native mobile app development

# Security Fixes

- 

# Bug Fixes

- Job offer acceptance automations amended to later stage trigger
- Console errors triggered when opening certain candidate profiles

# Developer Notes

-

## Test Coverage
- NewSearchScreenQuery replaced with stable SQL
- Gatling performance tests for Saved List candidate search

## Code Refactoring

- 

## Continuous Integration & Deployment

- 

## Cloud Enhancements

-

## Logging and Monitoring

-

## New Tools and Standards

- 

---

Thank you for using Talent Catalog! Your feedback and support are invaluable to us. If you encounter
any issues or have suggestions for improvement, please don't hesitate to [contact us](mailto:support@talentcatalog.net) or
[open an issue on GitHub](https://github.com/Talent-Catalog/talentcatalog/issues).

*[Access the latest version](https://tctalent.org/admin-portal/login)*
