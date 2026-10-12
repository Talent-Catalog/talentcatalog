# Matching: decoupling candidate matching from Job Opportunities — Decision Record

**Project:** Talent Catalog
**Issue:** TC-1552
**Date:** October 2026
**Status:** PR1–PR4 implemented. PR4 (explanations) awaiting review.

This document records the design of `Matching` and the decisions behind it. It replaces the
working plans (`matching-plan.md`, `matching-plan-v2.md`) used while the design was being agreed.
Where those plans differ from this document, this document wins.

---

## 1. Context

Candidate matching (semantic "BestN" search on natural language requirements, and AI generated
match explanations) used to be tied to Job Opportunities. Explanations were stored against
`candidateId + jobId`, and the job was effectively only used as a storage key and as the source
of the requirements text. That prevented matching and explanations from working for ordinary
searches and lists with no job.

`Matching` is a small entity representing a *matching context*: the natural language description
that candidates are matched against.

```java
Matching {
    Long id;
    String matchingDescription; // nullable
    OffsetDateTime updatedDate;
}
```

Deliberately not included: name, origin, status, owner, type, criteria JSON, or any other
ownership machinery. "Shared" simply means several candidate sources reference the same row.

## 2. Core principles

1. **A Matching represents a particular matching context.** Candidate sources share a Matching
   when they are deliberately taking part in the same matching activity. When the user
   deliberately creates a new persistent matching context, a new Matching is created, copying the
   current description where appropriate.
2. **`jobId` and `matchingId` are independent and additive.** `jobId` drives Job behaviour
   (opportunity filtering in lists, candidate opportunities, chats, Job UI). `matchingId` drives
   the natural language requirements and explanations only. A source may have neither, either or
   both. Changing the matching text never changes `jobId`, and `jobId` is never removed or
   repurposed.
3. **A Job has at most one Matching.** Candidate sources taking part in a Job's matching
   activity use that one Matching. No second Matching is ever derived from the same Job.
   (`salesforce_job_opp.matching_id` has a unique index, is read only in the entity, and is only
   set once, through `MatchingService.getOrCreateJobMatching`, which is safe under concurrency.)
4. **Job text only initialises a Job's Matching.** `extractJobText(job)` is a good initial
   description. After that the Matching has its own lifecycle: users refine it while searching
   (eg after talking to the employer), so `job description != matchingDescription` is valid.
   Later Job text changes never overwrite a non-blank Matching description. A Job's Matching
   may start blank (see section 6). When an operation explicitly needs the Job Matching's
   description and it is still blank, it is initialised from the Job's current text. There is
   no automatic synchronisation on Job updates.
5. **The default search is transient working state; saving is significant.** Explicitly saving
   a non-Job search, or saving candidates from a non-Job default search into a list, preserves the
   current context in a new Matching. This rule does not apply to Job searches, which share the
   Job's Matching.
6. **Never silently overwrite established context.** Fill in missing context where it is safe
   and meaningful.
7. **No backfill.** Matchings are created lazily, when an operation actually needs one.

## 3. Matching entity

- Table `matching` (V2_29): `id` from `matching_id_seq` (Matching extends `AbstractDomainObject`,
  for consistency with the rest of the domain model and its id based equality, instead of the
  IDENTITY strategy recommended for new tables), nullable `matching_description text`,
  `updated_date timestamptz not null`.
- `updateMatchingDescription(text)` normalises the text: anything without text content becomes
  null, including blank rich text such as `<p></p>` and `<p>&nbsp;</p>` (via Jsoup). It returns
  whether the description actually changed. `updatedDate` only changes when the normalised text
  changes.
- `updatedDate` is UTC, truncated to microseconds. Postgres stores microseconds while Java has
  nanoseconds, so without truncation a reloaded value would never equal the in memory snapshot
  used for explanation staleness.
- Angular uses the existing `hasTextContent` (`ui/admin-portal/src/app/util/string.ts`) for the
  same blank test. The server's `Matching.hasTextContent` is the backend equivalent.
- A Matching may have no description. A search with no description simply runs as a normal
  database filter search.
- Matchings are not deleted. Orphans (eg after a search is deleted) are harmless.

## 4. Lazy migration

- `saved_search.matching_id` (V2_30), `salesforce_job_opp.matching_id` (V2_30, unique) and
  `saved_list.matching_id` (V2_31) are all nullable, with indexes. No data is backfilled.
- Reading a Search, List or Job never creates a Matching.
- When a source needs a Matching:
  1. If it already has one, use it.
  2. Otherwise, if it has a Job, use the Job's Matching, creating it if the Job has none.
  3. Otherwise, for a Saved Search with legacy `requirements`, create its own Matching from
     them.
  4. Otherwise (eg an old list with no Job) no Matching is created without enough context
     and a concrete need.
- **Legacy `saved_search.requirements`:** the column is kept for the transition, because it
  holds the text needed to create Matchings for old searches. Once a search has a Matching, the
  Matching is authoritative: `SavedSearch.getRequirements()` returns the Matching's description,
  or the legacy value if there is no Matching yet. Legacy requirements are cleared whenever a
  Matching takes over. The column will be dropped in a later release.
- `SearchCandidateRequest.requirements` stays. It is the search run input; persisting a search
  request writes it to the Matching.

## 5. Searches (PR2)

- The Matching mapping lives on `AbstractCandidateSource`, shared by Saved Search and Saved
  List (moved there from `SavedSearch` in PR3).
- **Job searches** (default search opened with `?job=`, named Job searches, suggested searches)
  use the Job's single Matching. Editing their requirements edits that Matching. It does not
  change the Job description or the search's `jobId`, and it does not fork the Matching.
- **Saving a Job related default search** as a named search keeps the Job (the save search modal
  sends `jobId`) and therefore the same Matching. It does not become a non-Job search with a copy.
- **Non-Job searches** get their own Matching once they have requirements. An explicitly saved
  non-Job search is independent of later changes to the default search.
- **Changing a search's Job:** a search now associated with a Job uses that Job's single
  Matching. If the Job has no Matching yet, it is created from the search's current requirements,
  except for the default search, whose transient requirements are unrelated to the Job, so the
  Job's own text is used. A search whose Job is removed gets its own copy of the current
  description, so its later edits no longer change the old Job's matching context.
- **Resetting the default search** never blanks a Matching. A default search with a Job keeps
  the Job's Matching. One without a Job just drops its working Matching, and a new one is
  created when needed.
- **Opening `?job=`** (Angular `setUpJobMatch`) initialises the requirements from the
  server provided search requirements (the Job Matching's description) when they have text
  content, and only falls back to raw Job text when there is none. Simply opening a Job search
  never changes a refined Matching.
- `JobMatchingInfo.description` is the Job Matching's description if it has one, otherwise the
  extracted Job text.
- Search filters are completely separate from Matching.

## 6. Lists (PR3, and the follow up to PR3)

### Saved Lists and Submission Lists

`SavedList.registeredJob` identifies a **Submission List** (the property is not renamed yet):

- A Job may have many ordinary Saved Lists. Different teams or individuals may each keep their
  own, eg staff in Pakistan and Jordan sourcing candidates for the same Job.
- A Job has only one Submission List, shared by everyone providing candidates for that Job.
- `SavedListService.createSavedList` and `updateSavedList` only handle ordinary Saved Lists.
  `UpdateSavedListInfoRequest` has no `registeredJob` property, so no client request can
  create a Submission List or convert a list into one (an unknown `registeredJob` in the JSON
  is ignored). Ordinary Saved Lists for a Job can always be created, subject to the existing
  name rules.
- Submission Lists are only created by the server side Job workflow, through
  `SavedListService.getOrCreateSubmissionList(job)`: it returns the Job's existing Submission
  List, unchanged, or creates a new global one named after the Job. Job creation (including
  copied Jobs) and evergreen child Jobs use it. `createJob` can be given an existing Job via
  `createUpdateJob`, so it needs the idempotent form.
- A Job's Submission List is found by Job id (`SavedListRepository.findSubmissionList`),
  not by Salesforce id, since a new evergreen child gets its submission list before it has a
  Salesforce id.
- `getOrCreateSubmissionList` (one Submission List per Job) and `getOrCreateJobMatching` (one
  Matching per Job) are deliberately kept separate. They address different uniqueness
  requirements.

### Creating lists

- **A new list explicitly associated with a Job** gets the Job's single Matching straight away,
  creating it if necessary (`SavedListServiceImpl.createSavedList`). This covers lists created in
  the UI with a Job, new lists created from a Job search (the save dialog presets the Job from
  the source), the published `-suggest` list, and **Job submission lists**.
- Several ordinary lists for the same Job are distinct lists sharing the Job and its Matching.
- A new list without a Job gets no Matching at creation; it may inherit one from the candidates'
  source (below).
- An existing Submission List returned by `getOrCreateSubmissionList` is unchanged. Existing
  lists are not backfilled.
- **Copied Jobs and evergreen child Jobs** get their own new Matching (never the source Job's),
  initialised from the source Job's current Matching description, so refinements carry over.
  If the source has no description, the source Job's text is used. This happens when the new
  Job's submission list is created.

### Saving candidates into a list

`SavedListService.inheritMatching(destination, source)` applies when candidates are added to, or
replace the contents of, a list (save selection from a search, save matching search results,
copy or merge from a list):

- Only a **missing** Matching is filled in. An existing Matching is never replaced, because
  changing a list's candidates does not change what the list is about. That includes replacing
  its contents.
- A destination with a Job gets **its own Job's** Matching, never an unrelated source Matching.
- Otherwise it shares the source's Matching (a Job source supplies the Job's Matching), except
  that a **non-Job default search's** working Matching is copied, not shared, since it keeps
  changing as the user explores.
- A source with no Matching and no Job changes nothing.
- Selection lists are skipped. They always follow their search.

### Job association of existing lists

**Decision: existing lists inherit a missing Matching only, never a Job.** Adding candidates to
an existing list never changes its Job. A list with a Job only shows candidates who have an open
(or won) opportunity for that Job (`GetSavedListCandidatesQuery`), so giving an existing job-less
list a Job would hide its earlier candidates. New lists created from a Job search still get both
the Job and its Matching.

(The original PR3 brief proposed inheriting a missing Job too. That was revised to the above when
the opportunity filtering consequence was identified.)

### Selection lists

A selection list follows its search: it is created with the search's Job and Matching, and is
re-synced by id each time it is fetched, the same way `sfJobOpp` already was. Re-syncing never
creates a Matching.

### Saving matching search results

Angular sends `sourceSearchId` (not `sourceListId`) when the candidates come from a saved search.
The server first resolves the search's Matching (`SavedSearchService.resolveMatching`), then uses
the user's selection list for that search as the source list, creating it if needed. This also
fixed a bug: the search id used to be sent as `sourceListId` and looked up as a list id.

## 7. Explanations (PR4)

### Principles

- **A `CandidateMatchExplanation` belongs to a Matching and a Candidate** - nothing else. It does
  not depend on a Job, Saved Search or Saved List. Jobs, searches, lists (and future GRN
  services) reference Matchings; the explanation service only operates on Matching ids.
- **Explanations are a disposable cache of LLM output, not history.** At most one per
  (candidate, Matching). Generated individually, on demand - no batch generation. A current
  explanation is reused; a stale one is regenerated and replaced.
- **The server obtains the description from the Matching.** Angular never supplies it.

### Persistence

- `candidate_matching_explanation` (V2_32): PK `(candidate_id, matching_id)`, `matching_id` FK
  with `ON DELETE CASCADE` (plus an index on it), `matching_updated_date timestamptz not null`,
  `explanation jsonb not null` (same JSON structure as before), audit columns.
- `candidate_job_match_explanation` (V2_28) is dropped by V2_32. Its rows were discarded rather
  than migrated: the feature was unreleased, the rows were generated from text supplied by
  Angular with no record of which text, and mapping them would have needed a Job Matching
  backfill. V2_28 itself is unchanged.
- Entity, key, repository, mapper and jsonb data class renamed from `CandidateJobMatchExplanation*`
  to `CandidateMatchingExplanation*`. The entity is read only (`@Immutable`); rows are only
  written by the conditional upsert below, which also sets the audit columns.

### Staleness and concurrency

- Generation first takes a `MatchingSnapshot` - the description and `updatedDate`, read together
  from the database (`MatchingService.resolveDescription`). No transaction is held open during
  the LLM call.
- An explanation is current while its `matching_updated_date` equals the Matching's
  `updatedDate`. In Java this is compared with `isEqual` (instants), never `equals`, which also
  compares offsets. Precision is exact: `updatedDate` is truncated to microseconds (section 3).
- `CandidateMatchingExplanationRepository.saveIfMatchingUnchanged` is a single PostgreSQL upsert
  that only writes if the Matching's `updated_date` still equals the snapshot. It share locks the
  Matching row while saving, so the description can't change between the check and the write.
  So **an explanation generated from an older description is never saved**, and can never
  replace an explanation of the current description. Initial inserts and updates are handled by
  the same statement. Concurrent requests for the same pair are both saved; the last one wins,
  and both are equally current.
- If the Matching changed during generation, the result is discarded and the request is retried
  once from a new snapshot (first returning an explanation of the new description saved
  meanwhile by another request, if there is one). If it changes again, the request fails with a
  "please try again" error and nothing is saved. A bounded retry keeps the common case (one
  edit during generation) invisible to the user, without looping.
- **Candidate experience changes are not tracked.** Only Matching changes make an explanation
  stale. So the UI keeps a **Regenerate** action, which bypasses the cache (`regenerate: true`)
  and replaces the explanation, subject to the same concurrency protection.

### Blank descriptions

- A Matching with no description is rejected (400, `InvalidRequestException`), without calling
  the LLM.
- Section 2 principle 4 is applied in `MatchingService.resolveDescription`, not in the
  explanation service: if the Matching is a Job's and has no description, it is first
  initialised from the Job's current text. The Matching row is refreshed and locked first, so a
  description given meanwhile is never overwritten. Non-Job Matchings are never changed.

### API

- `POST /api/admin/candidate/{id}/match-explanation` with `{matchingId, regenerate?}` returns the
  current explanation, generating one if needed. The response structure is unchanged. The old
  `jobId` / `opportunityDescription` request properties and the `GET .../{jobId}` endpoint are
  gone (no backward compatibility, since unreleased).

### Matching resolution for Angular

- `POST /api/admin/saved-search/{id}/matching` and `POST /api/admin/saved-list/{id}/matching`
  return the source's `MatchingSnapshot` (`{id, matchingDescription, updatedDate}`), or an empty
  response if it has no matching context. They use `SavedSearchService.resolveMatching` and the
  new `SavedListService.resolveMatching`, then `MatchingService.resolveDescription`. POST, since
  they may create or change a Matching:
  - a legacy Job list (or Job search) with no Matching gets the Job's Matching;
  - a list with no Job and no Matching gets nothing;
  - a selection list returns its (search synced) Matching and never creates one.
- **Why endpoints rather than DTO fields.** Including the Matching in the source DTOs was
  considered, but was not practical: the id and `updatedDate` go stale when a search run creates
  or changes the default search's Matching; legacy lists need lazy creation, which reading a DTO
  must never do (section 4); a blank Job Matching needs initialising; and list DTOs are used for
  pages of lists (N+1). One small resolution call per source covers all of these.
- To avoid unnecessary calls and lazy creation on mere viewing, `show-candidates` only resolves
  the Matching when it is first needed - when a candidate is first selected (only the selected
  candidate's card shows explanations) - and then again after each run of a default or
  auto-update search, since running those saves their requirements.

### Angular

- `candidate-match-explanation` takes `candidateId`, `matchingId`, `matchingUpdatedDate` and
  `requirementsPendingMessage`. The click-to-show interaction is kept (nothing is requested
  until "Show match explanation" is clicked). States: closed, loading, loaded (with Regenerate),
  error (with "Try again"). A change of candidate, Matching or `updatedDate` discards the
  displayed explanation (and any request in flight) and returns to closed.
- The search card shows the explanation UI when the source's Matching has a description, no
  longer based on `sfJobOpp`.
- **Unsaved requirement edits.** When the requirements being edited on the search page differ
  (by text content, so editor reformatting doesn't count) from the Matching's saved description,
  no explanation is offered; instead the user is told to run the search (default and auto-update
  searches) or save it (other named searches). Explanations are never generated against text
  that differs from the persisted Matching.
- Removed: the `opportunityDescription` inputs, `explanationOpportunityDescription` and
  `loadSourceJobDescription` (Job text fallback) from `show-candidates`, and the four
  jobId/description modes of the explanation component.

## 8. Accepted consequences

- One Job Matching is shared by all of the Job's searches and lists, so editing the requirements
  in one changes them for all. A "detach" action can come later if needed.
- Concurrent edits of a shared Matching: last write wins.
- Old ordinary lists (not selection or Job lists) have no Matching, so no explanations, until
  candidates are saved into them from a source with matching context.

## 9. Implementation sequence

| PR | Scope | Status |
|----|-------|--------|
| 1 | Matching core: entity, repository, service, V2_29 | Done |
| 2 | Search lifecycle, Job Matching invariant, `?job=` and save search fixes (V2_30) | Done |
| 3 | List lifecycle: list Matching, inheritance, selection lists, `sourceSearchId` (V2_31) | Done |
| 3a | New Job lists and Submission Lists get the Job's Matching at creation; copied / evergreen Jobs start from the source Job's description; Submission Lists only created by `getOrCreateSubmissionList`, and `registeredJob` removed from list requests | Done |
| 4 | Explanations: persistence per Matching (V2_32), staleness, conditional upsert, Matching resolution endpoints, Angular explanation component and gating on Matching, `ShowCandidates` cleanup | Implemented, awaiting review |
| Later | Drop `saved_search.requirements`; consider making `salesforce_job_opp.matching_id` NOT NULL once every Job creation path creates one; consider tracking candidate experience changes for explanation staleness | To do |

## 10. Open items

- **Blank Job Matchings (decided; implemented in PR4 - see section 7).** A Job's Matching
  is created with its Submission List, usually before the Job has any text, so it often starts
  blank. Running a Job search fills it (Angular falls back to the Job text), and
  `JobMatchingInfo` falls back to the Job text meanwhile. Decision: an operation that explicitly
  needs the description initialises a still-blank Job Matching from the Job's current text;
  a non-blank description is never overwritten; no synchronisation is added to Job update
  operations. Explanation-specific resolution is part of the explanation PR.
- There is no database constraint enforcing one Submission List per Job; uniqueness relies on
  `getOrCreateSubmissionList` being the only way to create one.
- Order of operations in Clear Search.
- Creating a suggested search clears the default search.
- Avoid N+1 fetching of `matching` (fetch join in `findByIdLoadSearchJoins`; include `matching`
  only in the full search DTO).
- Unsaved requirement edits: implemented in PR4 (section 7).
- Candidate experience changes don't make explanations stale; users regenerate manually.
