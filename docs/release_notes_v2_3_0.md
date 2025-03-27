---
title: Release Notes 2.3.0
description: What's new in this release
permalink: v230
sass:
  style: compressed
---

# New Features

<div class="card-container">
  <a href="./v230/anonymized_open_api" class="card">
    <img src="./assets/images/v230/OpenApi.png" alt="Open Api" class="card-image">
    <div class="card-body">
      <div class="card-title">Anonymized Open API</div>
      <div class="card-description">
        Created an Anonymised Data Service API, so that API endpoints are clearly documented and accessible for 
        integration and usage by other services.
      </div>
      <div class="card-footer">
        <button class="btn btn-sm">Learn more</button>
      </div>
    </div>
  </a>

  <a href="./v230/tc_intelligence" class="card">
    <img src="./assets/images/v230/TCIntelligenceCard.png" alt="TC Intelligence" class="card-image">
    <div class="card-body">
      <div class="card-title">TC Intelligence with Preset</div>
      <div class="card-description">
        TC Intelligence brings the power of Preset to the Talent Catalog: analyse and present 
        advanced, interactive visualisations with real-time TC data!
      </div>
      <div class="card-footer">
        <button class="btn btn-sm">Learn more</button>
      </div>
    </div>
  </a>
</div>

<div class="card-container">
  <a href="./v230/duolingo_english_test" class="card">
    <img src="./assets/images/v230/DET.png" alt="Duolingo English Test logo" class="card-image">
    <div class="card-body">
      <div class="card-title">Duolingo English Test/TC Integration</div>
      <div class="card-description">
        Duolingo is providing candidates with free Duolingo English Tests. These tests can be assigned and managed 
        through the Talent Catalog.
      </div>
      <div class="card-footer">
        <button class="btn btn-sm">Learn more</button>
      </div>
    </div>
  </a>

  <div class="card">
    <img src="./assets/images/v230/MuteChat.png" alt="Mute chats" class="card-image">
    <div class="card-body">
      <div class="card-title">Mute Chats</div>
      <div class="card-description">
        TC admins can quietly 'mute' candidate chats, this is useful if a candidate is sending unnecessary chats which spams the chat channel.
      </div>
    </div>
  </div>
</div>

# User Guides

Helpful TC user guides:
<ul>
    <li>
        <a href="https://docs.google.com/document/d/1h5QaUNOSPP-pjJsMCDwXS_SQUrurvLfnBKPX87orgbE/edit?usp=sharing" 
        target="_blank">Employer Access User Guide</a>
    </li>
    <li>
        <a href="https://docs.google.com/document/d/1h5QaUNOSPP-pjJsMCDwXS_SQUrurvLfnBKPX87orgbE/edit?usp=sharing" 
        target="_blank">TC Chats Explainer Doc</a>
    </li>
</ul>


## General Improvements

- Email verification implemented for both candidate and admin users to reduce the number of bad or mistyped emails
- Option to 'Skip Candidate Search' on a job, this allows employers to indicate whether they want source partners to search for candidates or prefer to 'skip' and proceed only with candidates they’ve added to the submission list.
- Capture latest tab via the URL's tab parameter allowing for direct navigation to tab and will allow heat mapping on each tab
- Selection of stats in TC Stats (max 8) allowing stats to be run on the full database without performance issues
- Removed 'Run Old Stats' button from TC Stats as has been improved with the newer 'Run Stats' button
- Tasks have rich text descriptions so that links and formatting can be added to a task description
- Added relocated address fields (address, city, state, country) to candidate. Updatable on the candidate portal once the candidate has a case that is past the job offer stage. Can be updated anytime on the admin portal.
- Relocated country auto-populates with the destination country when a candidate's case is moved to or past the relocated stage.
- When a relocated address field is updated, a candidate note is created for tracking purposes and to help indicate if a relocated address is up to date.
- Candidates are automatically informed about Pathway Club (by email) when they have accepted a job offer
- Job titles now editable by owners and System Admins after publishing
- Option to set inactive partners' referral links to redirect to a nominated replacement
- Reassign candidates to new managing partner based on saved search results
- Submission List view now optionally displays the username and partner org of admin who added each candidate

## Data Improvements
- Expose Country ISO codes and Occupation ISCO codes
- New icon identifying potential duplicate candidate profiles, updating daily and in real-time by the resolution modal opened when icon clicked (HOTFIX)
- Housekeeping undertaken to align TC data with Salesforce Employer Opportunities
- Rename tbbShortName to tcShortName
- Rename task field 'helpLink' to 'docLink' as help links can now be linked to in rich text task descriptions (see #1829). Repurposed field to docLink to be used to display documents in iframe in tasks.
- Daily scheduled methods keep TC Opportunity data aligned with changes made on Salesforce
- Candidate relocated address replicated in Salesforce, dynamically displayed only if present
- Redundant Candidate Visa Job Check fields removed from DB

# UI / UX Enhancements
<div class="card-container">
  <a href="./v230/tc_redesign" class="card">
    <img src="./assets/images/v230/TCRedesign.png" alt="TC redesign" class="card-image">
    <div class="card-body">
      <div class="card-title">Sneak Peak into the TC redesign!</div>
      <div class="card-description">
        With the help of Hiba, our resident UI/UX designer, we are beginning a much desired upgrade to the TC user interface. 
        Incorporating Figma into our workflow, we can easily share our plans and early designs with you all.
      </div>
      <div class="card-footer">
         <button class="btn btn-sm">Learn more</button>
      </div>
    </div>
  </a>

  <div class="card">
    <img src="./assets/images/v230/ClarityHeatmaps.png" alt="Clarity heatmaps" class="card-image">
    <div class="card-body">
      <div class="card-title">Implemented heat mapping and screen recording</div>
      <div class="card-description">
        Part of the redesign process is gathering data on our users, we have implemented Clarity to provide and analyse heatmaps of user behaviour. 
        We look forward to sharing the results and using the results to provide data driven design choices.
      </div>
    </div>
  </div>
</div>


## Other UI / UX Enhancements

- Chat UI enhancement by separating chats by date sent and displaying initials of sender in icon
- Added warning not to enter confidential info into General Notes
- Candidate rows on submission lists now display the full name of the admin who add them
- Make Job Description more visible on Publish Job screen
- Enhanced cursor behaviour for clickable links
- More informative tool-tips by clarifying support text and UI simplification to assist employer users in creating jobs and using candidate search

# Performance Improvements

- Source partner Candidate Chats tab SQL refactored for faster load times
- Improved loading times for candidate profiles, both in list/search view and full profile view

# Security Fixes
- De-anonymisation of CV downloads restricted by partner type
- Display of Jobs data restricted by partner type

# Bug Fixes

- Fixed bug in NOT ListAll search filter
- Employer direct access does not see other employer jobs
- Removed TBB from non english translations and added missing partner references
- Admin-only DELETE endpoint reauthorised with recommended syntax for Spring Security 6
- Candidate search tooltips rewritten for Angular 16 compatibility
- Next Step column on submission lists displays correct value every time
- Fixed issue with candidate results not appearing on spreadsheet exports of search results
- Removed duplicated text in Opportunity modals
- Candidate email quick search made case-insensitive
- Candidate's associated with deleted or inactive partners are back appearing in searches
- Answers to question tasks are back appearing on admin portal
- Updating a search no longer clears the displayed multi-select selections
- Accepted offer notification now goes out even if the acceptance stage is skipped
- Next Step audit stamps replicated in Salesforce
- Character limit on Job naming to conform with Salesforce restrictions
- Submission list view infinite loading indicator issue resolved
- Candidate email search made case-insensitive
- Spring Boot Security request matchers pattern change
- Fixed tooltip display issue in candidate search
- Next Step column displaying correctly in Submission List view
- Candidates to only receive new chats notification email if the chats are unread
- Jobs tab on Candidate Portal only appears if there are opportunities past the <em>Prospect</em> stage, except for the closed stage <em>'Candidate was mistakenly proposed as a prospect for the job'.</em>

# Developer Notes
- Upgraded to Spring Boot 3
- Upgraded Angular to recent version
- MFA Authenticators now show TalentCatalog instead of TBB
- Updated copyright
- Deprecated Github Actions Upload Artifact version upgraded
- Moved from UA to GA4 tags in admin portal
- Standardized Terraform: <a href="https://drive.google.com/file/d/1JZUenkyr2rtVB7h8eElkRvXfwqbCIPQB/view?usp=drive_link" 
rel="noopener noreferrer nofollow" target="_blank">Terraform video 1</a>, 
<a href="https://drive.google.com/file/d/1kEiK77T8_krmqX-TxVodmNAJhDznbocA/view?usp=drive_link" 
rel="noopener noreferrer nofollow" target="_blank">Terraform video 2</a>, 
<a href="https://drive.google.com/file/d/1wLvXbuIZViKFnqJZd_R5HMsBVQrqhD_4/view?usp=drive_link"
rel="noopener noreferrer nofollow" target="_blank">Terraform video 3</a>.

## Code Refactoring
- Removed unused 'relocating dependants' field from visa job check, this was transferred to candidate opportunity in prior release.
- Removed unused candidate portal component - RegistrationLandingComponent

---

Thank you for using Talent Catalog! Your feedback and support are invaluable to us. If you encounter
any issues or have suggestions for improvement, please don't hesitate to [contact us](mailto:support@talentcatalog.net) or
[open an issue on GitHub](https://github.com/Talent-Catalog/talentcatalog/issues).

*[Access the latest version](https://tctalent.org/admin-portal/login)*
