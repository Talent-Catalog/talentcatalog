---
title: What's New in Talent Catalog
sass:
  style: compressed
---

## Version 2.2.1

This is an interim release broadly focussed on improving data and functionality requests that were 
not included in the previous release. The next major TC feature release will be version 2.3.0 in the 
summer.

## General Improvements
- Intake revisions - details here.
- Users can now build Elasticsearch queries on any base search, enabling the creation of reusable candidate searches. This
  saves time by allowing pre-filtering based on criteria like organizational countries of operation and immigration pathway eligibility.
- Password reset available to all users from the login screen - no longer needing an admin to do this.
- New search filter for mini and/or full intake completion ([link to video](https://app.screencastify.com/v3/watch/7oAheV8qgbgrbMlhQTvO))
- New 'Latest intake' column to display the latest intake completed. Hover over to see the date of completion for both mini and full intakes.
- New 'Latest intake date' column to display the date of completion of the latest intake.


## Data Improvements
- Next steps are now automatically appended with the username who made the change and the date when they did.
- Analysed duplicate candidate data registrations - less than 3% out of 104,000 reported registrations were identified as duplicates.


# UI / UX Enhancements
- Extended notification support (asterisks) for Chat messages
- Filtering of cases and jobs by unread messages
- Support for context sensitive help linking directly to existing process documents. This help can be country specific and can be updated by TC admin users as new help becomes available. (Help Links).
- Filtering of unverified candidates from reviewable search results
- Keep url even if a login is required
- Remember "show closed cases" user preferences on submission lists

## Other UI / UX Enhancements
- Google Analytics GA4 tags for candidate journey tracking
- Moved 'New Search' tab to front of the 'Searches' page tab group
- Fixed right hand side panel when browsing jobs & candidate opportunities, so that the top of the panel is still viewable when scrolling down the left hand side.
- More complete display of selected job information in right hand side panel
- Employer recruiters can only see candidate names after the candidate has reached the CV Review stage
- System keeps track of last active stage - even if case is closed.
- Added help to referer search field highlighting that wild card % can be used

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
- Resolved deprecated configuration in application YAML file


---

Thank you for using Talent Catalog! Your feedback and support are invaluable to us. If you encounter
any issues or have suggestions for improvement, please don't hesitate to [contact us](mailto:support@talentcatalog.net) or
[open an issue on GitHub](https://github.com/Talent-Catalog/talentcatalog/issues).

*[Access the latest version](https://tctalent.org/admin-portal/login)*
