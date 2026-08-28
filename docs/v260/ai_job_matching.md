---
title: AI Job Matching
description: The first pieces of AI-powered candidate matching, with more on the way
sass:
  style: compressed
---

# AI Job Matching: The First Steps Toward Smarter Candidate Search

Behind the scenes, Talent Catalog now runs a hybrid matching engine that combines
traditional text search with AI-generated vector embeddings — matching not just on exact
keywords, but on the meaning and context of a candidate's experience. This release brings the
first user-facing pieces of that engine online.

## 🔍 One Click From a Job Description to a Skill-Aware Search

Clicking the search icon on a job pulls the skills mentioned in that job's description and
pre-populates the Requirements field on the New Search screen with them, along with a note
showing which job they were extracted from. From there it's an ordinary search that can be
refined like any other — a first step toward a more general job search capability, without
taking away any existing search functionality. Eventually this kind of extraction is expected
to happen automatically as part of the everyday job search flow, rather than needing the
explicit click.

## 🎛️ Tuning a Requirements Search

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

The same search icon now appears next to every list name. Clicking it opens the standard
search screen pre-populated to show only candidates from that list — and from there, any
other filter can be layered on top, including a Requirements-based match.

## 🎯 Keyword Search Now Works Alongside AI Matching

A keyword search can be applied as an additional filter on top of a Requirements match, so
the two aren't an either/or choice. Requirements-matched results show a Score column (the
combined lexical/semantic score) in place of the old text-match Rank, sorted by that score;
sorting by any other column is disabled for these searches, since the point of a match is the
ranking itself. Because a match is a fixed top-N rather than an open-ended result set, an
"All to" button saves every matched candidate on the page straight into a list, without
needing to select them individually first.

## ⚙️ Under the Hood: A Hybrid Matching Engine

This release also lays the backend groundwork for AI matching: a Postgres schema storing
vector embeddings for candidate job experience, a batch process for building those embeddings
for existing candidates, and query logic that blends lexical and semantic scores according to
the configured weighting. Embedding models are pluggable — more than one can be registered,
each with its own configuration version and vector dimensionality — so the matching engine
isn't locked to a single provider or model going forward.

## 🚀 What's Next

- Automatic skill extraction as part of the everyday job search flow, rather than needing an
  explicit click.
- Continued tuning of match quality based on real usage.
