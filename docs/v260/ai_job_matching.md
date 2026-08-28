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

The search icon on a job extracts the skills mentioned in its job description and
pre-populates the New Search text area with them — a first step toward a more general job
search capability, without taking away any existing search functionality. Eventually this
kind of extraction is expected to happen automatically as part of the everyday job search
flow.

## 📋 Search Within Any List

The same search icon now appears next to every list name. Clicking it opens the standard
search screen pre-populated to show only candidates from that list — and from there, any
other filter can be layered on top, including AI matching itself.

## 🎯 Keyword Search Now Works Alongside AI Matching

A keyword search can be applied as an additional filter on top of AI match results, so
the two aren't an either/or choice.

## ⚙️ Under the Hood: A Hybrid Matching Engine

This release also lays the backend groundwork for AI matching: a Postgres schema and query
logic supporting hybrid text-and-vector search, and a batch process for building the vector
embeddings candidate experience is matched against.

## 🚀 What's Next

- Further tuning of match quality based on real usage is ongoing.
