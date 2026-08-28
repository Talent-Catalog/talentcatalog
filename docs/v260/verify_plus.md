---
title: Verify+
description: Scan your UNHCR Verify+ card to capture and verify your UNHCR registration number
sass:
  style: compressed
---

# Verify+: Scan Your UNHCR Card to Verify Your Registration

<div class="card-image-container">
    <img class="card-image"
         src="../assets/images/v260/VerifyPlusServicesCard.png"
         alt="UNHCR Verify+ card in the Candidate Portal Services tab"
    >
</div>

Refugees issued a UNHCR Verify+ card have their UNHCR ID encoded in a QR code on the card.
On GRN, candidates can now scan that code directly from their phone's camera to capture and
verify their UNHCR registration number — no backend account linking required, and nothing
ever leaves the device until the candidate chooses to submit it.

## 📷 Scan From the Services Tab

A new **Verify+** card in the Candidate Portal's Services tab lets a candidate scan their
UNHCR Verify+ QR code at any time. Selecting it opens the device's back camera; the code is
decoded on-device, and the scanned payload is shown for review before anything is submitted.

<div class="card-image-container">
    <img class="card-image"
         src="../assets/images/v260/VerifyPlusReviewScan.png"
         alt="Review scanned payload screen with Rescan and Confirm buttons"
    >
</div>

If the camera can't start — permission denied, or no camera on the device — the candidate
sees a clear message rather than a stuck screen. Once a scan succeeds, the camera switches
off automatically; a **Rescan** option is always available if needed.

On confirm, the scan is submitted and the candidate sees one of two outcomes:

- **Verification submitted** — "Your UNHCR number was captured successfully"
- **Duplicate UNHCR number found** — "We submitted your scan, but this UNHCR number already
  exists on another active candidate" — with the option to rescan

<div class="card-image-container">
    <img class="card-image"
         src="../assets/images/v260/VerifyPlusDuplicateFound.png"
         alt="Duplicate UNHCR number found error screen"
    >
</div>

## 📝 Or Scan During Registration

The same scan-and-submit flow is also offered as an optional, skippable step during GRN
registration — **"Scan your Verify+ card (optional)."** A candidate who scans their card has
their UNHCR registration number pre-filled automatically on the following step; a candidate
without a card handy can skip the step and continue registering.

<div class="card-image-container">
    <img class="card-image"
         src="../assets/images/v260/VerifyPlusRegistrationStep.png"
         alt="Optional Verify+ scan step during registration"
    >
</div>

Only the UNHCR registration number is captured this way — not name, date of birth, or any
other field. It shows up, already filled in, on the next step:

<div class="card-image-container">
    <img class="card-image"
         src="../assets/images/v260/VerifyPlusPrefilledField.png"
         alt="UNHCR registration number field pre-filled after a scan"
    >
</div>

If a different UNHCR number was already on file, the freshly scanned one takes its place.

## 🔍 Built for Real UNHCR QR Codes

Early testing surfaced a real-world snag: genuine UNHCR Verify+ cards use very high-density
QR codes — like the one below — that common JavaScript decoding libraries struggled to read
reliably. We investigated the failure and moved scanning onto a decoder capable of handling
that density, so a scan succeeds on the first realistic try rather than requiring several
attempts.

<div class="card-image-container">
    <img class="card-image"
         src="../assets/images/v260/VerifyPlusQrSample.png"
         alt="Sample UNHCR Verify+ card QR code"
    >
</div>

## 🌐 Available in Your Language

The entire Verify+ flow — scanner prompts, confirmation screens, success and duplicate
messaging — has been translated across all 11 languages the Candidate Portal supports,
including Arabic, Farsi, Pashto, and Ukrainian.

## ✅ Your Consent, Clearly Asked

This release also adds a consent step to the scan flow, on both the Services tab and
registration: before a card is submitted, the candidate must actively confirm they're happy
for it to be captured and stored — skipping the step means nothing is saved. The confirmation
screens are also being reworded to be clear about what a verified card actually proves: that
the card itself is genuine, not the identity of whoever is holding the phone.

## 👀 Coming Into View for Staff

Right after consent, work turns to giving admin-portal staff visibility into what's been
verified — showing the captured UNHCR ID and verification status on a candidate's profile,
so staff no longer have to take a candidate's word for it.

## 🚀 What's Next

- **Availability:** Verify+ is currently available on GRN only.
- **Beyond the UNHCR number:** today's release captures and verifies the UNHCR registration
  number only. Parsing and storing additional fields from the card, and photo handling, are
  both still ahead.
