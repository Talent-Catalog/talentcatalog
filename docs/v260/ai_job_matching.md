---
title: AI Matching
description: The first user-facing pieces of AI-powered candidate matching, with more on the way
sass:
  style: compressed
---

# AI Job Matching: The First Steps Toward Smarter Candidate Search

<!-- TODO(images): a timeline graphic for the top of this page, showing the evolution of TC
     matching by month/year — standard database search (origin) → Elasticsearch live (Jul 2020)
     → ChatGPT released / Gen AI age begins (late 2022) → Beamery & Deloitte AI matching
     collaboration begins (Apr 2025) → TC Postgres vector embeddings added (Aug 2025) → v2.4.0:
     Postgres text search replaces Elasticsearch, ranked results, ESCO/O*Net skill extraction
     (Nov 2025) → v2.5.0: major matching performance improvement (Apr 2026) → this release:
     full hybrid lexical + semantic AI matching. Over to our designer. 
     (See: https://linear.app/open-pathway-collective/project/ai-job-matching-b9d545893358/overview) 
     -- SM -->

## A Brief History of Matching in Talent Catalog

Matching has always been at the core of what Talent Catalog does, starting with our standard
database search screen — the goal has always been to match a job description to candidates
whose experience fits what's described. The first major advance was **Elasticsearch**, which went
live in July 2020 and made text search hundreds of times more efficient and powerful.

Then, in late 2022, ChatGPT arrived and the Generative  AI age began. By April 2025 we were 
working with Beamery and Deloitte to explore AI matching, and in August 2025 we added AI vector 
embedding support to the Talent Catalog's Postgres database. Vectors are at the core of all AI - 
in many ways they mimic the operation of neurons in the human brain, encoding the "meaning" of 
natural language text as numbers: multidimensional "vectors" that can be thought of as arrows 
pointing in directions in multidimensional space. Two pieces of text with similar meaning — say, 
a job description and a candidate's job experience — translate into vectors pointing in roughly 
the same direction, and can be compared using a technique called cosine matching.

In November 2025, v2.4.0 moved matching off Elasticsearch entirely and onto Postgres text
search, which also let us rank matches for the first time. That release also introduced
automatic skill extraction from job descriptions, built on the world's two largest skills
databases, ESCO and O*Net. April 2026's v2.5.0 then delivered a major performance improvement
across all matching.

Behind the scenes, Talent Catalog now runs a hybrid matching engine that combines that lexical                                                                                                                            
skills matching with "Beamery style" AI natural-language semantic matching — integrating with                                                                                                                             
all of our existing traditional search capabilities into a flexible, fast, and economical hybrid                                                                                                                          
matching capability. This release brings the first user-facing pieces of that engine online.

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

We're starting with one of the most popular open-source models, 
[all-MiniLM-L6-v2](https://huggingface.co/sentence-transformers/all-MiniLM-L6-v2), but
the architecture is designed to add and compare proprietary models too — from providers like
Google, OpenAI, and Anthropic — as they prove out.

## 🚀 What's Next

- Automatic skill extraction as part of the everyday job search flow, rather than needing an
  explicit click.
- Continued tuning of match quality based on real usage.
- Match explanations generated by LLMs (ChatGPT, Claude, Gemini, etc.), so users can see *why*
  a candidate was matched, not just that they were.
