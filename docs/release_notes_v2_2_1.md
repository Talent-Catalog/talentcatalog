---
title: What's New in Talent Catalog
sass:
  style: compressed
---

## Version 2.2.1

This is an interim release broadly focussed on Employer Access readiness for Iress and improving data 
and functionality requests that were not included in the previous release. The next major TC feature 
release will be version 2.3.0 in the next few months.

## General Improvements
- Intake revisions - [details here](v221/intake_revisions.md)
- Elasticsearch can now be built on regular base search - [details here](v221/elastic_base_searches.md)
- Password reset - [details here](v221/password_reset.md)
- New search filter for mini and/or full intake completion ([link to video](https://app.screencastify.com/v3/watch/7oAheV8qgbgrbMlhQTvO))
- Extended notification support and quick filtering for Chat messages - [details here](v221/extended_chat_notifications.md)
- File uploads via job chats for easy document sharing - [details here](v221/file_upload_job_chat.md)
- Highlighted search terms in search results - [details here](v221/search_highlight.md)
- **Google Analytics** migrated to GA4 for candidate registration journey tracking

## Data Improvements
- For **improved audit**, next steps are now automatically appended with the username who made the change and the date when they did.
- Analysed duplicate candidate data registrations - **less than 3% of 104,000 reported registrations were detected as duplicates**.


# UI / UX Enhancements
- More complete display of selected job information in right hand side panel - [details here](v221/job_side_panel.md)
- Filtering of unverified candidates from reviewable search results - [details here](v221/unverified_review_filter.md)
- Context Sensitive Help - [details here](v221/context_sensitive_help.md)
- Job chat emojis - [details here](v221/job_chat_emojis.md)
- The url that the user was trying to open is maintained even if a new login is required
- Remember "show closed cases" user preferences on submission lists

## Other UI / UX Enhancements
- Moved 'New Search' tab to front of the 'Searches' page tab group
- Fixed right hand side panel when browsing jobs & candidate opportunities, so that the top of the panel is still viewable when scrolling down the left hand side.
- Employer recruiters can only see candidate names after the candidate has reached the CV Review stage
- System keeps track of last active stage - even if case is closed.
- Added help to referer search field highlighting that wild card % can be used
- New 'Latest intake' column to display the latest intake completed. Hover over to see the date of completion for both mini and full intakes.
- New 'Latest intake date' column to display the date of completion of the latest intake.

# Fixed and Updated

## Security Updates
- Password reset available to all users from the login screen
- User URLs maintained across re-authorisation requests

## Fixes
- Remove button was not behaving correctly for submission lists - was not closing cases - fixed.
- Fixed bug where closed cases were sometimes being published in lists even when show closed cases
  was not checked.
- Fixed bug where My cases was not working - added better support for next step due.
- Fixed bug where overdue cases only was not saving. Added cases icon to fix problem of users not being able to quickly go to case.
- Fixed bug where candidate was seeing certain chats prematurely - eg before they had a job offer.
- Added submissionList icon to fix problem of users not being able to quickly go to submission list.
- Fixed bug where two identical chats were auto-created - resulting in an exception when fetching a chat because two were found instead of one.
- Fixed bug where the wrong type of opportunity was displaying when updating a job's progress.
- When adding a job description or interview guidance document or link to a job it wasn't appearing until page reload. This now appears straight after adding to job.
- Uploaded images on posts were appearing as broken links, fixed to display preview thumbnail of image.
- Fixed bug where displayed columns and description were being lost when saved search was updated.
- Fixed sorting by DOB column in search results coming from an Elasticsearch.

# Developer Notes

## Code Refactoring
- Resolved deprecated configuration in application YAML file.


---

Thank you for using Talent Catalog! Your feedback and support are invaluable to us. If you encounter
any issues or have suggestions for improvement, please don't hesitate to [contact us](mailto:support@talentcatalog.net) or
[open an issue on GitHub](https://github.com/Talent-Catalog/talentcatalog/issues).

*[Access the latest version](https://tctalent.org/admin-portal/login)*
