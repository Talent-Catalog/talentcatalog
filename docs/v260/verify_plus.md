---
title: Verify+
description: TODO
sass:
  style: compressed
---

# Verify+

TODO — introduce UNHCR Verify+ QR-code scanning. Do not describe photo handling,
retention/erasure, or consent-gating — none of that has shipped yet (see TC-1364, TC-1365,
TC-1349, and epics TC-1346/TC-1347, all not Done).

## Scanning Your Verify+ Card

TODO — TC-1355 (QR scanner component + Services card), TC-1356 (confirm and submit scanned
payload to backend), TC-1363 (backend ingest endpoint, captures UNHCR ID). Also covers the
scan-reliability improvement for high-density UNHCR QR codes (TC-1392) and the translation/
localisation pass (TC-1375).

## Verify+ During Registration

TODO — TC-1357: optional, skippable scan step during registration that pre-fills name, DOB,
and UNHCR number from a scanned card.
