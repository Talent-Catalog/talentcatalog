# ADR 004: Candidate Registration Email Paste Sanitization — Trim over Invisible-Character Stripping

**Date:** 2026-08-26
**Status:** Accepted

## Context

TC-723 (#578): candidates copy-pasting their email address into the registration
form sometimes see "This field must contain a valid email" even though the
address is correct. The candidate portal validates email fields
(`registration-create-account.component`, `registration-contact.component`)
against a custom regex (`EMAIL_REGEX` in `ui/candidate-portal/src/app/model/base.ts`),
applied as an HTML5 `pattern` attribute. Because `pattern` validation requires the
*entire* value to match, any extra character carried along by a paste — visible or
not — causes an otherwise-valid address to fail.

Investigation found no existing trimming/sanitization anywhere in the registration
path (frontend `tc-input` component, `AuthenticationService`) or on the backend
(`BaseCandidateContactRequest.email` had no validation or normalization at all).

A first fix stripped a broad set of invisible/blank Unicode characters — not just
plain whitespace — from the leading/trailing edges of the pasted value only,
leaving the middle of the address untouched (since a stray character mid-address
indicates a genuinely malformed email, not a paste artifact). This was driven by
reproducing the reported case with a **Hangul filler character (U+3164)**, which
is invisible on screen but is *not* covered by standard whitespace definitions in
either language runtime:

- JavaScript's `\s` covers ordinary whitespace plus every Unicode "space
  separator" (Zs) character and the byte-order mark (U+FEFF).
- Java's `\s` (and `String.strip()`, via `Character.isWhitespace()`) covers only
  ASCII whitespace and a narrower Zs subset — it does not include non-breaking
  space, ideographic space, or the BOM the way JavaScript's does.
- Neither language's built-in whitespace/strip logic covers zero-width
  characters (`​`-`‍`), bidi marks, word joiners, or the Hangul/Khmer
  filler-character family (`ᅟ`, `ᅠ`, `឴`, `឵`, `ㅤ`,
  `ﾠ`) — Unicode classifies the latter as *letters*, despite rendering
  blank.

A regex-based sanitizer (`INVISIBLE_OR_WHITESPACE_CHAR`, using `\p{Zs}`/`\p{Cf}`
plus the six explicit filler codepoints) was built and kept byte-for-byte
identical across the frontend (`ui/candidate-portal/src/app/model/base.ts`) and a
backend port (`server/.../request/candidate/BaseCandidateContactRequest.java`),
each verified against the other in a Node/jshell cross-check.

## Decision

Use plain **`.trim()` (frontend) / `.strip()` (backend)** on the email field,
dropping the custom invisible-character regex.

## Reasoning

### Scope tradeoff, made explicitly
Plain trim/strip only catches ordinary whitespace and the small set of
"space separator" characters each language already recognizes as
whitespace — it does **not** catch zero-width characters, bidi marks, or the
Hangul/Khmer filler family, including the exact character (U+3164) from the
original bug report. This is a known, accepted regression in scope versus the
regex-based version: pasting a Hangul-filler-prefixed email will fail validation
again, the same as before any fix existed.

### Consistent with observed industry precedent
Manually testing Google's own email input (signup flow) with a pasted invisible
character showed the same scope of behavior we're adopting: leading/trailing
whitespace-like characters are trimmed, but if the resulting value still isn't a
valid email address, Google surfaces a validation error rather than silently
accepting it or attempting to strip characters from within the address. This
matches our decision to trim at the edges and let genuinely malformed input
continue to fail validation, rather than trying to guess and clean up every
possible invisible character a user might paste.

### Simplicity wins for the common case
The overwhelming majority of real-world paste artifacts are ordinary leading/
trailing whitespace (a stray space from copying out of an email client, a
trailing newline). `.trim()`/`.strip()` are one-line, dependency-free, and
require no cross-language Unicode-category reasoning to maintain — unlike the
regex version, which needed careful verification (jshell/Node) every time either
side was touched, because Java's and JavaScript's definitions of `\s` do not
match.

### Maintenance cost of the regex version was disproportionate
Keeping the frontend and backend character classes in sync, correctly accounting
for behavioral differences between `\s` in JS versus Java, and testing every
individual codepoint's Unicode category classification, added meaningfully more
surface area than the problem justified for a low-severity, infrequently-hit
validation-message bug.

## Consequences

- `sanitizeEmailInput()` (`ui/candidate-portal/src/app/model/base.ts`) is now
  `value == null ? value : value.trim()`, still used from both
  `registration-create-account.component.ts` and
  `registration-contact.component.ts`, so the entry point and its
  TC-723-referencing comment are preserved even though the implementation is now
  trivial.
- `BaseCandidateContactRequest.setEmail()`
  (`server/.../request/candidate/BaseCandidateContactRequest.java`) now calls
  `email.strip()`, giving `SelfRegistrationRequest` and other subclasses the same
  backstop with no format validation elsewhere on the backend.
- Test suites (`base.spec.ts`, `registration-create-account.component.spec.ts`,
  `registration-contact.component.spec.ts`, `BaseCandidateContactRequestTest.java`)
  were trimmed to cover only trim/strip behavior — clean-value passthrough,
  leading/trailing whitespace, a middle-embedded space left untouched, and
  empty/null safety — removing the per-codepoint Unicode-category tests that no
  longer reflect what the code does.
- If the Hangul-filler (or similar invisible-character) case resurfaces as a
  live complaint rather than a one-off report, the regex-based
  `INVISIBLE_OR_WHITESPACE_CHAR` approach documented in this ADR's history is a
  ready-made reference for reinstating broader coverage.

## Alternatives Considered

| Option | Outcome |
|---|---|
| No sanitization (status quo before TC-723) | Rejected: leaves the reported bug unfixed. |
| Full invisible-character regex (`\p{Zs}`/`\p{Cf}` + explicit filler codepoints), mirrored frontend/backend | Rejected: fixes the reported case but adds ongoing cross-language maintenance cost disproportionate to the bug's severity. |
| `.trim()` (frontend) / `.strip()` (backend) (chosen) | Accepted: fixes the common whitespace-paste case with minimal, easily maintained code; knowingly does not fix the specific Hangul-filler case from the original report. |
