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

- #2220 Possible fix of memory leak as side effect of this issue. Protecting against memory loss due 
to not closing streams. 
- #2129 Fixing answers not displaying properly in Candidate Portal profile 
- #1129 Candidate Portal registration: English ability and professional certification questions 
marked as required but not actually required to proceed 
- #2380 CV Generation Fails Due to Invalid Characters in Contact Info 
- #2217 Source partners should not be able to change the opportunity stage of opps they are not 
responsible for 
- #2213 Unable to Identify Duolingo Coupon Redemption 
- #2041 Hide Send Duolingo Test task from Tasks list
- #2106 Search by survey type not working as expected
- #2030 Add “HowHeardAboutUs” enum & update candidate source options in Talent Catalog
- #2580 Can clear a phone number field when using international phone input on admin portal
- #2192 Fix partner and role logic which was hiding certain fields from partners (e.g. UNHCR)
- #2575 New search functionality SQL references deleted 'major_id' field
- #2548 Clickable tooltip in Update Candidate Opp modal not working
- #2532 New search functionality SQL incorrectly references potential duplicates field
- #2414 Same-value logging emissions from chat subscriptions
- #2264 Publish Job sets Salesforce counterpart to wrong stage
- #2350 Candidate TC created date incorrectly translated to SF Contact record
- #2286 List publish error
- #2177 Erroneous Next Step automated chat post
- #281 environment.staging.ts not utilised
- #1847 Default Next Step for newly created Candidate Opp not reflected in SF
- #1887 Education Level (minimum) filter not working
- #2122 Remove null characters from Link Previews
- #2079 Duplicate audit stamp and default Next Step for newly created Job not saving to TC DB
- #2074 Job Opp Next Step not updating
- #2589 Download of autogenerated CV not working for candidate 289669 on production
- #2568 Potential bug: candidate note records change to 'pending' status but status still 'draft
- #1129 Candidate Portal registration: English ability and professional certification questions 
marked as required but not actually required to proceed
- #966 Error accessing unknown fields on JPA entity during active session

# Developer Notes

## Test Coverage

- #2392 Create DuolingoExamServiceImplTest.java
- #2391 Create DuolingoExtraFieldsServiceImplTest.java
- #1667 Write Unit Tests for Duolingo Coupon Service
- #2148 Enhance Tests for Low-Coverage Components in Admin Portal
- #2149 Improve Test Coverage for Admin Portal Directives
- #2400 Testing: GoogleFileSystemServiceImpl
- #2139 Enhance Test Coverage for org.tctalent.server.api.admin Package
- #2211 Extensive per-resource test data
- #2410 GDPR process verification
- #2398 Testing: DuolingoApiServiceImpl

- // TODO -- capture coverage stats as the coverage % has massively increased

## Code Refactoring


## Continuous Integration & Deployment


## Cloud Enhancements


## New Tools and Standards

- #2418 Simply configured Spring Batch based solution for processing candidates in a list or search
- #2418 Spring Batch version of old BackgroundProcessor which adapts batch processing depending on
  how busy system is.
- #2165 Additional reporting via Preset dashboard on candidate UTMs and params for TBB comms



---

Thank you for using Talent Catalog! Your feedback and support are invaluable to us. If you encounter
any issues or have suggestions for improvement, please don't hesitate to [contact us](mailto:support@talentcatalog.net) or
[open an issue on GitHub](https://github.com/Talent-Catalog/talentcatalog/issues).

*[Access the latest version](https://tctalent.org/admin-portal/login)*
