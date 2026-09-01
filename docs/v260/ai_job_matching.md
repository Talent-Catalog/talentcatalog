---
title: AI Matching
description: The first user-facing pieces of AI-powered candidate matching, with more on the way
sass:
  style: compressed
---

# AI Job Matching: The First Steps Toward Smarter Candidate Search

Behind the scenes, Talent Catalog now runs a hybrid matching engine that combines
traditional text search with AI-generated vector embeddings — matching not just on exact
keywords and search filters, but on the meaning and context of a candidate's experience. This 
release brings the first user-facing pieces of that engine online.

## 🔍 One Click From a Job Description to a Skill-Aware Search

<!-- TODO(images): a shot of a job's search icon (view-job screen), or better, the New Search
     screen right after clicking it — Requirements field populated with the job's skills and
     the "extracted from <job name>" note visible underneath. SM -->

Clicking the search icon on a job pulls the skills mentioned in that job's description and
pre-populates the Requirements field on the New Search screen with them, along with a note
showing which job they were extracted from. From there it's an ordinary search that can be
refined like any other — providing a powerful and general AI job search capability, without
taking away any existing search functionality. Eventually this kind of extraction is expected
to happen automatically as part of the everyday job search flow, rather than needing the
explicit click.

## 🎛️ Tuning a Requirements Search

<!-- TODO(images): a screenshot of the full Requirements panel — Lexical vs Semantic slider,
     Number of matches, and Model Key with its configuration version/name/dimensions showing. 
     SM -->

Whenever at least one embedding model is available, the New Search screen shows a
Requirements field: free text describing the candidate experience being looked for — a job
description, or anything similar. Once it's filled in, three more controls appear:

- **Lexical vs Semantic matching** — a slider from 0 to 1 controlling how much weight is
  given to keyword matches in the Requirements text versus semantic similarity from vector
  embeddings. It defaults to 0.5, weighting the two equally.
- **Number of matches** — how many candidates to return, defaulting to the current page size.
- **Model Key** — which registered embedding model to match against, with its configuration
  version, name (linked to more detail), and vector dimensionality shown alongside.

## 📋 Search Within Any List

<!-- TODO(images): a shot of the search icon next to a list name in the lists view — with 
     a single arrow/circle callout. SM -->

The same search icon now appears next to every list name. Clicking it opens the standard
search screen pre-populated to show only candidates from that list — and from there, any
other filter can be layered on top, including requirements based AI matching.

## 🎯 Keyword Search Alongside AI Matching

<!-- TODO(images): a results table screenshot with a keyword filter also visible. SM -->

A keyword search can be applied as an additional filter on top of an AI requirements-match, so
the two aren't an either/or choice.

## ⚙️ Under the Hood: A Hybrid Matching Engine

This release also lays the backend groundwork for AI matching: a Postgres schema storing
vector embeddings for candidate job experience, a process for building those embeddings
for existing candidates, and query logic that blends lexical and semantic scores according to
the user's configured weighting. Embedding models are pluggable — more than one can be registered,
each with its own vector dimensionality — so the matching engine isn't locked to a single 
provider or model going forward.

## 🚀 What's Next

- Automatic skill extraction as part of the everyday job search flow, rather than needing an
  explicit click.
- Continued tuning of match quality based on real usage.
