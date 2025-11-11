---
title: Release Notes 2.4.0
description: Improved Matching Algorithm
sass:
  style: compressed
---

## Prioritised Matching

Results of a search that contains keywords logic is now sorted by closeness of match.

<div class="card-image-container">
    <img src="./../assets/images/v240/SortedMatchResults.png" 
    alt="Search results sorted by closeness of match" class="card-image">
</div>


## Generate a Search Directly from a Job Description

Click on the search icon for any job...

<div class="card-image-container">
    <img src="./../assets/images/v240/JobWithSearchIcon.png" 
    alt="New search icon on a job" class="card-image">
</div>

The TC will scan all text related to the job - uploaded job description, job summary, job intake etc 
extracting skills from the text.
We have a database of around 30,000 skills extracted from the [ESCO](https://esco.ec.europa.eu/en/about-esco/what-esco) 
and [O*NET](https://www.onetcenter.org/taxonomy.html) collections.

The extracted skills are automatically added to a New Search which searches for candidates with those skills.

<div class="card-image-container">
    <img src="./../assets/images/v240/NewSearchWithExtractedSkills.png" 
    alt="New search with extracted skills" class="card-image">
</div>

You can add or remove skills if you wish. You can also modify the Keyword search as usual to 
construct boolean expressions - for example requiring this skill AND that skill.


## Highlight Search Keyword Matches in Uploaded CVs

Keyword search matches are now not only shown highlighted in the entered Experience and Education 
data...

<div class="card-image-container">
    <img src="./../assets/images/v240/HighlightSearchKeyword.png" 
    alt="New search icon on a job" class="card-image">
</div>

... but keyword matches are also shown in any uploaded Cvs

<div class="card-image-container">
    <img src="./../assets/images/v240/HighlightSearchKeywordInCv.png" 
    alt="New search icon on a job" class="card-image">
</div>

## Improved Text Search Infrastructure

This release introduces the use of **PostgreSQL’s full-text search extensions** to power keyword 
search logic within the Talent Catalog.  

Postgres now handles text indexing and relevance scoring which can be used for more efficient 
candidate profiles and uploaded CVs searches.

Previously, text search was handled through **Elasticsearch**. By consolidating this capability 
into Postgres, we can:

- Simplify the system architecture by removing a separate Elasticsearch service
- Reduce maintenance overhead and streamline data consistency
- Save approximately **US $2,500 annually** in licensing and hosting costs

This change is an important step toward **fully retiring Elasticsearch** in a future release, 
and handling all core search capabilities natively within Postgres instead.
