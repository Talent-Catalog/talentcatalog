---
title: Release Notes 2.4.0
description: What's new in this release
permalink: v240
sass:
  style: compressed
---

# New Features

<div class="card-container">
  <a href="./v240/tc_ui_redesign" class="card">
    <img src="./assets/images/v240/UiRedesign.png" alt="UI Redesign" class="card-image">
    <div class="card-body">
      <div class="card-title">User Interface Redesign -- Phase I</div>
      <div class="card-description">
        The Talent Catalog is getting a fresh new look! In this release we are introducing the first 
        phase of our UI redesign, incorporating the new TC styling across the Admin portals.
      </div>
      <div class="card-footer">
        <button class="btn btn-sm">Learn more</button>
      </div>
    </div>
  </a>

  <a href="./v230/italy_train_to_hire" class="card">
    <img src="./assets/images/v240/ItalyTrainToHire.png" alt="Italy Train to Hire" class="card-image">
    <div class="card-body">
      <div class="card-title">Italy Train to Hire Support</div>
      <div class="card-description">
        This release streamlines refugee document collection and verification for Italian 
        train-to-hire candidates, so that individuals can manage their own data while improving 
        coordination and reducing errors across agencies.
      </div>
      <div class="card-footer">
        <button class="btn btn-sm">Learn more</button>
      </div>
    </div>
  </a>
</div>

<div class="card-container">
  <a href="./v230/privacy_agreements" class="card">
    <img src="./assets/images/v240/PrivacyAgreementReview.png" alt="Privacy Agreement Review" class="card-image">
    <div class="card-body">
      <div class="card-title">Privacy Agreements Review - GDPR compliance</div>
      <div class="card-description">
        We have updated our privacy agreements for an individual partner's compliance with GDPR 
        regulations. Admins can now track candidate consent status and candidates are prompted to 
        review and accept updated agreements upon login.
      </div>
      <div class="card-footer">
        <button class="btn btn-sm">Learn more</button>
      </div>
    </div>
  </a>

  <a href="./v230/improved_matching" class="card">
    <img src="./assets/images/v240/ImprovedMatching.png" alt="Improved Matching" class="card-image">
    <div class="card-body">
      <div class="card-title">Improved Matching</div>
      <div class="card-description">
        Job match results are now sorted by relevance, helping admins focus on the best-fit 
        candidates and speed up their review process.
      </div>
      <div class="card-footer">
        <button class="btn btn-sm">Learn more</button>
      </div>
    </div>
  </a>

</div>

<div class="card-container">
  <div class="card">
    <img src="./assets/images/v240/ApiRevisions.png" alt="TC API Revisions" class="card-image">
    <div class="card-body">
      <div class="card-title">TC API Revisions</div>
      <div class="card-description">
        This release enhances the TC API with improved batch process management, refined 
        functionality, and increased alignment with international data standards.
      </div>
    </div>
  </div>

  <div class="card">
    <img src="./assets/images/v240/CasiFramework.png" alt="CASI Framework" class="card-image">
    <div class="card-body">
      <div class="card-title">CASI Framework</div>
      <div class="card-description">
        This release delivers the backend framework for the Candidate Assistance Services Interface 
        (CASI), enabling future partner-led, data-driven support and engagement for Talent Catalog 
        candidates.
      </div>
    </div>
  </div>

  <div class="card">
    <img src="./assets/images/v240/SkillsExtraction.png" alt="Skills Extraction" class="card-image">
    <div class="card-body">
      <div class="card-title">Skills Extraction</div>
      <div class="card-description">
        This release lays the groundwork for integrating globally recognized ESCO and ONET skills 
        into candidate profiles, with AI-assisted extraction to improve data quality and candidate 
        visibility.
      </div>
    </div>
  </div>

</div>

# User Guides



## General Improvements

- #2499 Improved use and display of candidate properties
- #2136 Support for versioned privacy agreements and automated approval processes. New Candidate 
Registration process to support new agreements. Support for separate candidate agreements with each 
managing source partner (not just TBB)
- #2280 New GDPR compatible candidate privacy agreement 
- #2375 Display date that privacy terms were accepted by candidate 
- #1939 Warning to candidate about CV related content could be made publicly viewable. 
- #2190 Hotfix 2.3.0 Added WhatsApp number to the export feature of searches 
- #2528 New Candidate Status: RelocatedIndependently 
- #1885 Added WhatsApp numbers to candidate quick search by phone 
- #2191 Added country code to phone number inputs on admin portal 
- #1984 Removal of candidate.migration_nationality from TC database and entities 
- #2690 Update CASI header misleading name

## Data Improvements

- #2440 Slovakia added to TC countries 
- #2130 Email alert for failing scheduled background batch operations

# UI / UX Enhancements


## Other UI / UX Enhancements


# Performance Improvements


# Security Fixes

- #2482 Discontinue TC->SF all-candidates sync

# Bug Fixes


# Developer Notes


## Code Refactoring


## Continuous Integration & Deployment


## Cloud Enhancements


---

Thank you for using Talent Catalog! Your feedback and support are invaluable to us. If you encounter
any issues or have suggestions for improvement, please don't hesitate to [contact us](mailto:support@talentcatalog.net) or
[open an issue on GitHub](https://github.com/Talent-Catalog/talentcatalog/issues).

*[Access the latest version](https://tctalent.org/admin-portal/login)*
