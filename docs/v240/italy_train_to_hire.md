---
title: Release Notes 2.4.0
description: Italy Train to Hire Data Collection Support
sass:
  style: compressed
---

# Overview
This release introduces targeted improvements to support the Italy train-to-hire program, addressing 
critical government data requirements and reducing error-prone manual processes. The updates empower 
candidates to take ownership of their information and support smoother coordination between TBB, 
UNHCR, and government agencies.

## Why This Matters
To complete a conditional job offer in Italy, refugee candidates and their relocating family members 
must submit the following to Italian authorities:

- Travel document scans
- Evidence of **Refugee Status Determination (RSD)**
- Refugee ID (if applicable)

These documents are reviewed by **three separate government agencies**, and even minor data 
inconsistencies can result in **significant delays or abandoned cases**. Until now, TBB and UNHCR have 
manually transcribed data from scanned documents into spreadsheets — a slow, error-prone process 
exacerbated by mismatches between candidate profiles and submitted documents.

This release addresses those issues by applying core **Talent Catalog principles**: data 
self-management by candidates, minimal redundant requests, and a single system of truth.

## Key Updates in This Release

**✅ Candidate-Driven Data Entry via TC Tasks**

A structured task flow enables candidates to upload and verify the required documents and fields 
themselves, reducing dependence on manual follow-up by TBB or UNHCR.

> 🆕 This flow is powered by the new [Forms Tasks framework](forms_tasks.md), built specifically to 
> support the complex data collection needs of the Italy train-to-hire program. Forms Tasks allow 
> candidates to complete multi-field submissions directly within a task — improving accuracy, 
> reducing friction, and streamlining future use cases.

**Task Breakdown:**

1. Candidate Travel Document Upload
2. Relocating Family Member Document Upload
3. Upload of RSD Evidence & Refugee ID
4. Upload of RSD & Refugee ID for Family Members

Where documents or data are missing, Tasks guide candidates to complete what’s needed, only when 
it becomes necessary — not at registration.

**📤 Improved Bulk Export for Government Submission**

To support case processing across agencies, uploaded documents and extracted data can be exported 
in bulk for a given cohort using the existing List publishing features.
