---
title: Release Notes 2.4.0
description: CASI Framework
sass:
  style: compressed
---

# Launching the CASI Framework: Candidate Assistance Services Interface

This release introduces the backend framework for the **Candidate Assistance Services Interface 
(CASI)** — a new integration layer designed to help partner organisations deliver targeted, 
data-informed support to candidates registered on the Talent Catalog.

## What is CASI?

CASI enables authorised partners such as, for example, **UNHCR, IOM, migration lawyers, resettlement 
agencies, and others** to offer tailored services to candidates based on their Talent Catalog 
profile data.

These services could include:

- 🎓 **Language testing** (e.g., Duolingo)

- 💬 Candidate engagement with **automated chats and follow-up communication**

- 📊 **Opt-in/out job opportunity tracking**

- 📌 **Automated engagement tasks**

- 🛠 **Alumni community building and Pathway Club membership**

- 📍 **Candidate location mapping** using geo location mapping tools

- 🧭 **Personalised candidate advice** and information relevant to the candidate's present 
circumstances

## What’s New in This Release

The **CASI backend framework** has now been implemented to support this expanding ecosystem of 
services. It standardises how new services are integrated with the Talent Catalog backend, making 
it easier and faster for developers to build and deploy support tools for eligible candidates.

## Key Features:

- 🔌 **Decoupled from Task Logic:** Services like Duolingo can now be managed directly, and do not 
need to be done using tasks.

- ⚙️ **Automatic Task Generation:** Admins can assign services (e.g., language testing coupons) in 
bulk, and CASI automatically handles task creation to track progress.

- 🧱 **Built for Expansion:** Future CASI services can be layered onto this foundation, with 
consistent APIs and lifecycle hooks.

## Front-End: Powered by Forms Tasks

On the candidate-facing side, CASI services can leverage the new **[Forms Tasks](forms_tasks.md)** 
framework to guide candidates through data capture or participation — ensuring a consistent, 
self-service experience for those deemed eligible.

> 💡 With CASI and Forms Tasks working together, partners can engage candidates more proactively 
> while reducing manual admin overhead.

---

This foundational release enables scalable, automated candidate support — with more CASI-based 
services to be added in upcoming releases.
