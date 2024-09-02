---
title: What's New in Talent Catalog
sass:
  style: compressed
---

## Version 2.2.2

Check out the newest features and enhancements.

# New Features

<div class="card-container">

  <a href="./v220/employer_access" class="card">
    <img src="./assets/images/v220/EmployerAccess.png" alt="Employer Access" class="card-image">
    <div class="card-body">
      <div class="card-title">New way of running stats on saved searches</div>
      <div class="card-description">
        New more efficient Run Stats button, able to run stats for > 30,200 candidates.
      </div>
    </div>
 </a>

  <a href="./v220/job_chats" class="card">
    <img src="./assets/images/v220/JobChats.png" alt="Job Chats" class="card-image">
    <div class="card-body">
      <div class="card-title">Automated Messages</div>
      <div class="card-description">
        Job Chats introduce a unified way for Talent Catalog users to communicate. Employers, 
destinations, source partners and candidates can all communicate regarding job opportunities and 
candidate relocation directly from the Talent Catalog.
      </div>
    </div>
  </a>

</div>

<div class="card-container">

  <a href="./v220/candidate_data_in_salesforce" class="card">
    <img src="./assets/images/v222/CandidateChats.png" 
            alt="Candidate Data in Salesforce" class="card-image">
    <div class="card-body">
      <div class="card-title">Candidate Chats</div>
      <div class="card-description">
        Manage all active chats with candidates in one location with the new Candidate Chats tab.
      </div>
    </div>
  </a>

</div>

## General Improvements
- Update and sync relocating dependants to SF on the candidate case or on the candidate's visa check
- New UK visa check, containing the relocating dependants update & SF sync.
- Create a new job using a previous job as a template, this will copy over any job data that can be duplicated
- Upload a signed MOU document to a candidate case
- All fields now available to be used with 'Keyword Search' (which utilises elasticsearch functionality) in candidate search, which also increases the utility of its 'Base Search' functionality (NB: I'll add a video for this one - SS)
- Email validation improved and fully aligned across components and portals
- 'UNHCR Status' added as filter in candidate search
- Translation added to Admin Portal for improved code-sharing with Candidate Portal and to facilitate future non-English TC admin
- The "read only" for any user has been reviewed and now should work well for any kind of user. Read only users can create and modify their own lists and searches - but they can't change anything else.
- Salesforce errors appearing on admin portal to confirm if a Salesforce update has or hasn't been successful
- Destination preferences moved from Mini Intake and added to the candidate's registration process on the candidate portal. On the admin portal this data appears and is editable under the General tab of the candidate's profile.

## Data Improvements

- Salesforce Candidate Opportunity relocating dependants stats now auto-updated at 'Relocated' stage
- Support added for new Salesforce 'MOU' and 'Training' stages
- Elasticsearch index updated in all data interactions with candidates
- Candidates' managing partner can now be reassigned in bulk from Settings > System Admin API, improving efficiency and data integrity
- Consolidate UNHCR Registered and UNHCR Status fields, now stored in a single UNHCR Status field which provides most informative and up to date data.


# UI / UX Enhancements

<div class="card-container">

  <div class="card-no-border">
    <img src="./assets/images/v220/EmployerReadyNavigation.png" alt="Employer-ready Navigation" class="card-image">
    <div class="card-body">
      <div class="card-title">Employer-Ready Navigation</div>
      <div class="card-description">
        We've reorganised the TC main menu and sub-menus to make it easier for new and existing 
users to navigate. The main menu is now simply: <em>Jobs</em>, <em>Searches</em>, <em>Lists</em>, 
and <em>Stats</em>. Clicking on Jobs navigates to all things jobs related, Searches for all things 
search related, and so on. <a href="https://tctalent-test.org/admin-portal/login" target="_blank">
Login to our staging environment</a> to take a look.
      </div>
    </div>
  </div>

  <div class="card-no-border">
    <img src="./assets/images/v220/WidenedColourPalette.png" alt="Widened Colour Palette" class="card-image">
    <div class="card-body">
      <div class="card-title">Widened Colour Palette</div>
      <div class="card-description">
        With this release we bid farewell to the old bright blue and teal colour scheme, which did
not contrast terribly well against the white background. In some cases leading to eye strain over 
prolonged periods of use. We hope the new muted yet strongly contrasted colours will be 
significantly more accessible for all. Here's John with a 
<a href="https://drive.google.com/file/d/1ONFcuvZxKZKwEUcn9wmbBNM5t2c-5e8B/view" target="_blank">
quick video</a> introduction.
      </div>
    </div>
  </div>

</div>

## Other UI / UX Enhancements

- Fix overlapping navbar when scrolling on candidate portal
- Better alignment of content under tabs in candidate portal
- AU visa check has updated style consistent with other visa checks
- Added subtle box shadow to buttons to provide extra dimension on admin portal
- Proper hover-over link behaviour added to Candidate Portal
- Tooltip added to help users in updating candidate lists
- Candidate search profile card restores previous scrollbar position for new candidate selections
- 'Copy list' and 'save selection to list' pop ups have improved layout
- Job Chat notification * now appears as a red dot consistent with notification UI on other apps
- Neatened the filter checkboxes for job and opportunity searches
- Updated the mark as read button and removed if a chat is already read
- Disabled buttons appear greyed out to help active buttons stand out on the page
- Candidate search card fills the window height


# Security Fixes

- Added MFA to the AWS Production environment for an additional layer of security


# Bug Fixes

- Visa job checks can now only be created for the job destination's visa check
- Candidate fields no longer being overwritten by older cached candidate - this resolves candidate fields such as intake data, partner and status reverting back to an older cached value
- Weekly candidate syncing to Salesforce now fully operational
- Performance issues arising from looped processing of large result sets resolved using Entity Manager's clear() method
- Fixed translation display issues in Candidate Portal registration
- CVs now downloading successfully from the candidate search results dropdown menu and clickable icon
- Candidate Prospect chats no longer being unnecessarily auto-created on candidate profile view, instead created via a button in the tab with help text; viewable by all appropriate admins
- Search tips no longer erroneously opened on 'Enter' keydown in candidate search inputs
- Candidate search correctly handling default saved search for new TC users
- Validation tightened and informative error-handling added to Job publishing from Salesforce URL
- IELTS scores of 0 now displayed in candidate search results
- Help text clarified on Admin Portal date picker component
- Potential password exposure in server logs removed
- Pop ups with a 'select list' dropdown made larger to avoid list names being cut off


# Developer Notes

## Test Coverage

- This release expands our unit test coverage by ~30% across various components, including
  JobAdminApi, PartnerAdminApi, UserAdminApi, CandidateStatAdminApi, and more, with incremental
  effects on code quality and system robustness.
- The introduction of Gatling/Scala performance testing harnesses to the TC build lifecycle, will
  now execute routine system and database performance tests, for the early detection of any
  performance issues or bottlenecks. Forthcoming releases will build further on this.

## Code Refactoring
- Deprecated candidate visa job check fields removed from code
- Emoji picker code refactored for better readability

## Continuous Integration & Deployment
- Pushing changes to developer branches will now trigger a full build and a complete cycle of unit
  testing and performance testing - failure alerts will be notified to contributors for the early
  detection and resolution of breaking code.
- We've introduced [hot-fix branching](https://github.com/Talent-Catalog/talentcatalog/wiki/Release#hotfix-branches)
  which is to be used for out-of-band quick fixing of production issues.

## Cloud Enhancements
- Elasticsearch's versions have been upgraded from the end-of-life v7.12 to v7.17.11.
- The elasticsearch index has been rebuilt and the old v7.12 clusters have been safely
  decommissioned.
- Elasticsearch production clusters have been migrated to the US for proximity to the TC cloud
  hosting, which also resides in the US. Co-locating both services will alleviate unnecessary
  transatlantic network hops when running elastic searches, with improved network search performance.
- Postgres DB version 11 reaches end of life on AWS in February 2024, and has therefore been
  upgraded to Postgres DB version 14.7 with this release.
- Data storage has been upgraded to AWS provisioned storage, which is AWS's recommended data storage
  type for Talent Catalog production data.
- Last but not least, we've upgraded to a 4 CPU RDS database to allow the application to scale with
  peak system usage.



---

Thank you for using Talent Catalog! Your feedback and support are invaluable to us. If you encounter
any issues or have suggestions for improvement, please don't hesitate to [contact us](mailto:support@talentcatalog.net) or
[open an issue on GitHub](https://github.com/Talent-Catalog/talentcatalog/issues).

*[Access the latest version](https://tctalent.org/admin-portal/login)*
