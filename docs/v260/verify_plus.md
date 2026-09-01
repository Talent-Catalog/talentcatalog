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
verify their UNHCR registration number and personal details — no backend account linking required, 
and nothing ever leaves the device until the candidate chooses to submit it.

## 📷 Scan From the Services Tab

A new **UNHCR Verify+** card in the Candidate Portal's Services tab lets a candidate scan their
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


The above screen grab is just a mocked scan, and actual QR scan decodes a candidate's personal
details, as well as their UNHCR registration number and a low res photo image may also be decoded
from their UNHCR Verify+ card.

## 📝 Or Scan During Registration

The same scan-and-submit flow is also offered as an optional, skippable step during GRN
registration — **"Scan your Verify+ card (optional)."** A candidate who scans their card has
their UNHCR registration number and personal details pre-filled automatically on the following 
step; a candidate without a card handy can skip the step and continue registering.

<!-- TODO(images): The VerifyPlusRegistrationStep.png carries over a hint banner — "If possible
     please provide this response in English so it can be more easily reviewed by our team"
     — that's boilerplate text for a free-text question and doesn't fit a QR-scan step.
     It would be better not to display it -- needs a dev fix.  SM -->

<div class="card-image-container">
    <img class="card-image"
         src="../assets/images/v260/VerifyPlusRegistrationStep.png"
         alt="Optional Verify+ scan step during registration"
    >
</div>

The UNHCR registration number is captured, alongside any personal details,and it shows up, 
already filled in, on the next registration step , for candidate review:

<div class="card-image-container">
    <img class="card-image"
         src="../assets/images/v260/VerifyPlusPrefilledField.png"
         alt="UNHCR registration number field pre-filled after a scan"
    >
</div>

## 🔍 Built for Real UNHCR QR Codes

Genuine UNHCR Verify+ cards use very high-density QR codes — like the sample below — and common 
decoding libraries struggle to read these QR codes reliably. Therefore GRN mobile scanners are 
built using decoders capable of handling that density, so a scan succeeds on the first realistic 
try.

<div class="card-image-container">
    <img class="card-image"
         src="../assets/images/v260/VerifyPlusQrSample.png"
         alt="Sample UNHCR Verify+ card QR code"
    >
</div>

## 🌐 Available in Your Language

The entire UNHCR Verify+ flow — scanner prompts, confirmation screens, success and duplicate
messaging — has been translated across all 11 languages the Candidate Portal currently supports,
including Arabic, Farsi, Pashto, and Ukrainian.

<!-- TODO(images): Add a multi-language image.  SM -->

## ✅ Your Consent, Clearly Asked

This release also adds a consent step to the scan flow, on both the Services tab and
registration: before a card is submitted, the candidate must actively confirm they're happy
for it to be captured and stored — skipping the step means nothing is saved.

<!-- TODO(images): Grab a screenshot nce the consent PR is merged.  SM -->

## 👀 Coming Into View for Staff

Right after consent, admin-portal staff have visibility into what's been verified — showing 
the captured UNHCR ID and verification status on a candidate's profile.

<!-- TODO(images): Grab a screenshot once the admin portal PR is merged.  SM -->

## 🚀 What's Next

- **Availability:** Verify+ is currently available on GRN only.
- **Beyond the UNHCR number:** today's release captures and verifies the UNHCR registration
  number only. Parsing and storing additional fields from the card, and photo handling, are
  both still ahead.
