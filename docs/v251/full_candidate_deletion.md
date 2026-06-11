---
title: Full Candidate Deletion
description: Stronger candidate data deletion support
sass:
  style: compressed
---

# Full candidate deletion

Talent Catalog now supports full candidate deletion for cases where a candidate requests that their data is removed.

---

### Supporting candidate privacy rights 

Candidates may request that all of their data is removed from Talent Catalog.

This release improves support for this workflow and helps Talent Catalog meet stronger privacy and GDPR data protection requirements.

---

### Deletion status remains the preferred option

In most cases, marking a candidate status as deleted remains the preferred approach.

This preserves historical information for reporting, statistics, and future operational context, while ensuring the candidate is no longer shared or treated as active in normal workflows.

Full candidate deletion is intended for the smaller number of cases where a candidate specifically requests that all of their data be removed.

This gives administrators a supported way to complete full data deletion without needing manual database changes.

---

### Handling connected candidate data safely

Candidate data can appear in connected structures such as lists, opportunities, notes, documents, and profile text.

The deletion workflow is designed to remove candidate data while avoiding broken database relationships or unsafe admin flows.

---

### Extra confirmation for destructive actions

Because full deletion is destructive and irreversible, the implementation requires stronger confirmation before the process can continue.

<div class="card-image-container">
  <img src="../assets/images/v251/FullCandidateDeletionConfirmation.png"
        alt="Full candidate deletion" class="card-image">
</div>
