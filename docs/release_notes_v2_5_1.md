---
title: Release Notes 2.5.1
description: What's new in this release
permalink: v251
sass:
  style: compressed
---

# New Features

## Candidate Data Management and Task Notifications

The release introduces important improvements to how candidate information is managed and how users are notified about assigned tasks. Candidate-entered job experience text can now be kept separate from admin-enhanced text, full candidate deletion is now supported, and generic email alerts can now be sent when Tasks are assigned.

<div class="card-container">

  <a href="./v251/job_experience_text_preservation" class="card">
    <img src="./assets/images/v251/JobExperienceTextPreservation.png" alt="Job Experience Text Preservation" class="card-image">
    <div class="card-body">
      <div class="card-title">Separate Candidate and Admin Job Experience Text</div>
      <div class="card-description">
        Candidate-entered job experience text and admin-enhanced text can now be kept separate, helping preserve the candidate's original words while still supporting profile improvements.
      </div>
      <div class="card-footer">
        <button class="btn btn-sm">Learn more</button>
      </div>
    </div>
  </a>

  <a href="./v251/full_candidate_deletion" class="card">
    <img src="./assets/images/v251/FullCandidateDeletion.png" alt="Full candidate deletion" class="card-image">
    <div class="card-body">
      <div class="card-title">Full Candidate Deletion</div>
      <div class="card-description">
        Full candidate deletion is now supported, helping Talent Catalog meet stronger privacy and data protection requirements.
      </div>
      <div class="card-footer">
        <button class="btn btn-sm">Learn more</button>
      </div>
    </div>
  </a>

  <a href="./v251/task_email_alerts" class="card">
    <img src="./assets/images/v251/TaskEmailAlerts.png" alt="Task email alerts" class="card-image">
    <div class="card-body">
      <div class="card-title">Task Email Alerts</div>
      <div class="card-description">
        Generic email alerts can now be sent when Tasks are assigned, helping candidates and users stay informed about required actions.
      </div>
      <div class="card-footer">
        <button class="btn btn-sm">Learn more</button>
      </div>
    </div>
  </a>

</div>

## Other Enhancements
* Toggle page size for candidate results

# User Guides

# General Improvements

* Elasticsearch can now be decommissioned with an annual saving to TBB.
*  Mini intake: Citizenships - intake screen changes
* Updating Step 8 destinations in Candidate Portal.
* Saved List capability for TC Intelligence 
* Extend CASI to support service lists

# Data Improvements

# UI / UX Enhancements

*  Display folder name on Candidate profile > Additional Info tab > uploaded files

## Other UI / UX Enhancements

# Performance Improvements

# Security Updates

* Look at and fix version warnings in gradle build file
* Explicitly adds missing public paths for Spring Security 6
* Hardcoded JWT Secret, Database Password, and Translation Password (DS-009)
* Add audit fields to all candidate-provided data tables #2856 Add audit fields to all candidate-provided tables
* Remove hardcoded defaults for Bootstrap Users (dead code path but triggers code scan alerts)
* Added an upper and lower bounds to page size of pagination to avoid risk of memory leaks
* Remove remaining files related to old Elasticsearch functionality
* Renamed spring security authority role from 'ReadOnly' to 'Restricted' to more accurately reflect the API end point security authorities
* Protected System Admin endpoints by preauthorising user is System Admin and changed state changing endpoints to PUT requests from GET requests
* Add candidate authguard to LinkedinPortalApi 

# Bug Fixes

* Bug where candidates on GRN are marked as ineligible at the end of the registration process if they are already located in a destination country
* Aspirations do not appear in candidate-portal summary
* Application startup fails on empty database due to duplicate partner primary key insertion
* Fix shared resource assignments unique constraints on CASI
* Added null guard to citizenship intake panel
* Resolved migration issue causing some candidates not to be loaded

# Developer Notes

* Moved to new open source licence
* Access to s3 attachments (staging only) for GRN devs
* Access to distinct s3 translations for devs running GRN / TBB instances
* Updated README to fix bug where a new user setting up will require a password environment variable

## Test Coverage
* Add tests for candidate-related service classes

## Code Refactoring

## Continuous Integration & Deployment

* Auto restart ECS services after CI image deployments
* Enable GitHub code scanning for the Talent Catalog repositories

## Cloud Enhancements

* Standardise environment variables across all Terraform deployments
* Configure TBB AWS tasks for new translation service

## Logging and Monitoring

## New Tools and Standards

## Compliance, Security, and Platform Standards

This release strengthens the platform’s security and compliance foundations, including Vanta integration, GitHub code scanning, Spring Security updates, and removal of hardcoded or outdated configuration.

<div class="card-container">

  <a href="./v251/vanta_aws_integration" class="card">
    <img src="./assets/images/v251/VantaAwsIntegration.png" alt="Vanta AWS integration" class="card-image">
    <div class="card-body">
      <div class="card-title">Vanta Integration</div>
      <div class="card-description">
        Vanta integration supports compliance monitoring, security evidence collection, and improved visibility across connected infrastructure.
      </div>
      <div class="card-footer">
        <button class="btn btn-sm">Learn more</button>
      </div>
    </div>
  </a>

</div>

## Other Enhancements 
*  Linear - Phase 1 - prep + decisions
*  Linear - Phase 2 - setup + developer onboarding

---

Thank you for using Talent Catalog! Your feedback and support are invaluable to us. If you encounter
any issues or have suggestions for improvement, please don't hesitate to [contact us](mailto:support@talentcatalog.net) or
[open an issue on GitHub](https://github.com/Talent-Catalog/talentcatalog/issues).

*[Access the latest version](https://tctalent.org/admin-portal/login)*
