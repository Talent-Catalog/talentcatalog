-- Candidate match explanations are now cached per (candidate, Matching) rather than per
-- (candidate, job). Explanations are disposable LLM output from a feature that has not been
-- released, so existing job keyed explanations are discarded rather than migrated.
create table if not exists candidate_matching_explanation (
    candidate_id          bigint      not null references candidate (id),
    matching_id           bigint      not null references matching (id) on delete cascade,
    -- The Matching's updated_date when generation started: the explanation is current while
    -- this equals matching.updated_date.
    matching_updated_date timestamptz not null,
    explanation           jsonb       not null,
    created_date          timestamptz,
    created_by            bigint references users (id),
    updated_date          timestamptz,
    updated_by            bigint references users (id),
    primary key (candidate_id, matching_id)
);

create index if not exists candidate_matching_explanation_matching_id_idx
    on candidate_matching_explanation (matching_id);

drop table if exists candidate_job_match_explanation;
