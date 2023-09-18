---
title: Release Notes
sass:
  style: compressed
---

<link rel="stylesheet" type="text/css" href="{{ site.baseurl }}/assets/css/styles.css">

# Release Notes for Talent Catalog

## Version 2.1.0 (September 29, 2023)

### New Features

- Candidate opportunities supported in admin portal
- Candidate jobs visible in candidate portal
- SF integration - automatic updates without the need for any button press
- Canada visa eligibility checks - first iteration
- Support for candidate opportunity stages

### Improvements

- All search fields are now available for elastic searching - including occupation, eduction level and eduction major
- Adds support for default destination partners as well as default source partners

### UX / UI Enhancements

- Candidate opportunities page for ease of single candidate opportunity viewing
- Enhanced Job icon and introducing the terms Cases to refer to Candidate Opportunities
- Wider easy-to-read candidate sidebar profiles in candidate lists view


### Security Fixes

- Strong visual indication distinguishing production, staging and local environments

### Bug Fixes

- Candidate profile audit fields correctly updating
- Deleting a Referrer in a new search properly clears and resets the search field
- Clean SF data to ensure correct handling of opportunities where country name is given as just CA (instead of Canada)
- Allow deleting of published lists
- Deleting work experience redirects to log-in screen - fixed

### Developer Notes

- AWS cloud cpu increased to 0.5 per task
- Unit test coverage for AuthAdminApi, BrandingAdminApi, CandidateAdminApi
- Master and staging branch and PR protection rules enforced 
- Refactored common code out of job and candidate opportunities
- Transitioned GitHub repository to an organisation account


---

Thank you for using Talent Catalog! We appreciate your feedback and support. If you encounter any issues or have suggestions for improvement, please [contact us](mailto:support@talentcatalog.org) or [open an issue on GitHub](https://github.com/Talent-Catalog/talentcatalog/issues).

*[Access the latest version](https://tctalent.org/admin-portal/login)*
