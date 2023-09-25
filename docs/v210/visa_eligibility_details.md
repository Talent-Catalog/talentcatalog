---
title: Canada Visa Eligibility Checks
sass:
  style: compressed
---

# Canada Visa Eligibility Checks


This release introduces an updated iteration of visa eligibility checks, which was previously designed and used for 
Australian visa checks only. The new functionality has been introduced to support comprehensive Canada 
visa eligibility, but has been designed with all other destinations in mind. This will allow us to perform an efficient 
rollout of this visa eligibility check functionality across all destinations in future releases of the TC.

The functionality allows users to perform visa eligibility checks from within the TC, encompassing initial checks, 
job-specific questions, and standardized final assessment criteria. Conducting visa eligibility checks on the TC allows 
for easy access of the data that is required to effectively perform the check by displaying data from the intakes,
candidate registration, job opportunity intake or any other useful data that exists on the TC. Each destination will 
have their own set of questions and data that is displayed, however the overall design and functionality will be 
standardised (as per what is shown in this release).


# Section 1 - General Visa Assessment Questions

The first section of the visa eligibility check collects data that is relating to the **candidate** and the 
**destination country**. Keeping these questions in their own section allows them to be answered once and avoids 
unnecessary data duplication.

<div class="card-image-container">
  <img src="./../assets/images/v210/CanadaVisaCheckSectionOne.png" alt="Enhanced Other Languages Search" class="card-image">
</div>

# Section 2 - Job Specific Assessment Questions

The second section of the visa eligibility check collects data that will change depending on the particular job 
opportunity. Data collected are job specific, such as relevant experience for the role, the job's location, 
candidate's interest in the role etc. When adding a job to the visa eligibility check the candidate's job 
opportunities appear as selections.

<div class="card-image-container">
  <img src="./../assets/images/v210/CanadaVisaCheckSectionTwo.png" alt="Enhanced Other Languages Search" class="card-image">
</div>

# Notable functionality

Some of the helpful functionality of the visa eligibility check:
- **Auto save:** Data is saved as it is typed (indicated by the icons to the right of the question). 
  - The keyboard icon indicates the data is being entered
  - The loading icon indicates the data is being saved
  - The floppy disk icon indicates the current entered data is saved
- **Useful links:** Links are displayed to help in data collection for example linking to government website listing immigration pathways or to the job's listing on the TC.
- **Collated data:** The data that is needed to perform the visa eligibility checks can be collated from all areas of the TC. This data can then be and displayed as is or modified to provide the most value. 
- **Reusability:** The database architecture allows the data to be reusable throughout the TC, for example to search/filter by candidates who have had visa eligibility checks conducted.
- **User experience:** Using radio buttons and accordions to reduce the total page size to allow easy navigation and nicer experience on smaller screens.

<Link to Caroline's video's>

