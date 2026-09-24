# ADR 005: Our Postgres Database standards

**Date:** 2026-09-24
**Status:** Review

## Context
  
Our original database was created back in 2019 and used some old Postgres conventions.
Those conventions have changed over time. 
This decision documents some changes to more recent conventions we should start using.

- IDENTITY used for ids rather than BIGSERIAL
- TEXT used for text fields rather than VARCHAR(n) unless there is clear limit

## Decision



## Reasoning
    
### IDENTITY vs BIGSERIAL
Starting with PostgreSQL 10, Identity columns were introduced as a more reliable and flexible 
alternative to Serial. 
Identity columns behave similarly to Serial, but with better guarantees about sequence management 
and more control over how the values are generated.

See, for example, 
[this article](https://java-jedi.medium.com/the-evolution-of-primary-keys-in-postgresql-from-serial-to-identity-and-beyond-f62662bc2595).

### TEXT vs VARCHAR
Text is not standard SQL - it is a Postgres extension. However, it does appear to be recommended by
Postgres which recommends TEXT or VARCHAR without a limit. But VARCHAR without a limit is not
standard SQL or universally supported (for example, it would be an error in MySQL). 
Adding an arbitrary length limit seems pointless.
The proposal is to only use VARCHAR when the limit is significant. For example, 
a two-character country code should be VARCHAR(2) 
(note that Postgres recommends VARCHAR(2) over CHAR(2)). 
Otherwise, use TEXT.

See [Postgres documentation](https://www.postgresql.org/docs/current/datatype-character.html).

## Consequences
           
Once this decision is accepted, we should add the recommendations to the AGENTS.md file.

## Alternatives Considered
