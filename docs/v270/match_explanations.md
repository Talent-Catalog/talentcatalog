---
title: Match Explanations
description: AI-generated explanations of why each candidate matches a job
sass:
  style: compressed
---

# Match Explanations: Why Was This Candidate Matched?

[v2.6.0](https://talent-catalog.github.io/talentcatalog/v260) brought AI matching online, ranking
candidates by how closely their experience fits a job description. It also promised that users
would be able to see *why* a candidate was matched, not just that they were.

This release delivers that. For any candidate in a matching search or a job's submission list,
Talent Catalog can now generate a plain-English explanation of how well the candidate's
experience fits the job — written by a large language model (LLM).

---

## 🎬 Watch the Videos

New to AI matching in Talent Catalog? These short videos are a quick orientation to how it works.

### Getting Started with AI Matching (Part 1)

<div class="card-image-container">
  <iframe src="https://www.youtube-nocookie.com/embed/2vEAV6BP05M"
          title="Getting Started with AI Matching (Part 1)"
          style="width: 100%; max-width: 720px; aspect-ratio: 16 / 9; border: 0;"
          allow="accelerometer; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
          allowfullscreen></iframe>
</div>

[Watch on YouTube](https://www.youtube.com/watch?v=2vEAV6BP05M)

### AI Matching: Best of Both Worlds (Part 2)

<div class="card-image-container">
  <iframe src="https://www.youtube-nocookie.com/embed/j31wSDvwHro"
          title="AI Matching: Best of Both Worlds (Part 2)"
          style="width: 100%; max-width: 720px; aspect-ratio: 16 / 9; border: 0;"
          allow="accelerometer; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
          allowfullscreen></iframe>
</div>

[Watch on YouTube](https://www.youtube.com/watch?v=j31wSDvwHro)

### Behind the Scenes of AI Matching (Part 3)

<div class="card-image-container">
  <iframe src="https://www.youtube-nocookie.com/embed/93V5SrPc60k"
          title="Behind the Scenes of AI Matching (Part 3)"
          style="width: 100%; max-width: 720px; aspect-ratio: 16 / 9; border: 0;"
          allow="accelerometer; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
          allowfullscreen></iframe>
</div>

[Watch on YouTube](https://www.youtube.com/watch?v=93V5SrPc60k)

## 📝 Start From the Requirements

![AI match requirements on the New Search screen](../assets/images/v270/MatchExplanationRequirements.png)

Explanations compare a candidate against the **AI match requirements** text on the search screen.
That might be a job description pulled in from a job (as in the example above), or anything you
type yourself describing the experience you're looking for.

## 👁️ Show a Match Explanation

![Show match explanation link on the candidate card](../assets/images/v270/MatchExplanationShow.png)

Select a candidate in the search results and click **Show match explanation**.

Explanations are only generated when you ask for one, so simply running a search or browsing
results never triggers any AI processing.

## 💡 What an Explanation Contains

![A match explanation](../assets/images/v270/MatchExplanation.png)

Each explanation includes:

- **A summary** of how well the candidate matches overall.
- **Relevant experience** — a separate assessment of each of the candidate's job experiences,
  headed by that experience's job title.
- **Limitations** — what the candidate's experience doesn't show, or where the information is
  too thin to judge.

Explanations are dated, and show the name of the LLM that generated them (a Qwen model in this
case). Click **Regenerate explanation** to generate a fresh one — for example after the candidate
has updated their experience.

Both parts of our hybrid approach bring general world knowledge to the comparison. In this example
the candidate never actually mentioned “cloud” experience, but she did mention AWS. The vector
embeddings that made the match knew that AWS is a cloud platform, and so did the LLM that
explained it. The embeddings find the right candidates quickly across the whole database, and the
LLM then explains each match in plain language, so each does the job it’s best at rather than
asking an LLM to do everything.

## 🔎 See the Experience Behind Each Explanation

![Hovering over an experience shows the candidate's description of it](../assets/images/v270/MatchExplanationExperienceHover.png)

Hover over any experience heading to see the candidate's own description of that experience,
including dates, role type and location, with search keywords highlighted. The popup stays open
while you move into it, so long descriptions can be scrolled.

## 📋 Explanations on Submission Lists

![A match explanation on a job's submission list](../assets/images/v270/MatchExplanationSubmissionList.png)

When a search is associated with a job, each candidate's explanation is saved against that job.
Next time anyone shows the explanation for that candidate and job — from a search or from the
job's submission list — the saved explanation is displayed straight away, with no new AI call.

New or updated explanations can also be generated directly from a job's submission list, using
the job's description as the requirements.

Searches that aren't associated with a job still show explanations, but these are generated on
request and not saved.

## ⚠️ A Word of Caution

Match explanations are generated by AI. They are a quick way to understand a candidate's fit,
but they can misread or overlook information. Use them as a guide alongside the candidate's
profile, not as a substitute for reviewing it.

## ⚙️ Under the Hood

- Explanations are generated by the Talent Catalog AI service, currently using the open-weight
  **Qwen3 235B** model. The cost is roughly 30 explanations per US cent.
- Only the requirements text and the candidate's job experience (job title and description) are
  sent to the LLM — not the candidate's name, contact details or other profile information.
- Saved explanations are stored per candidate and job, recording when they were generated and
  which model generated them. Regenerating an explanation replaces the saved one.
