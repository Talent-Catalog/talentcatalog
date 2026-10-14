-- Nullable: existing searches and jobs get their Matching lazily, when one is needed.
alter table saved_search add column if not exists matching_id bigint references matching (id);
create index if not exists saved_search_matching_id_idx on saved_search (matching_id);

-- A Job has at most one Matching, and a Matching belongs to at most one Job.
alter table salesforce_job_opp add column if not exists matching_id bigint references matching (id);
create unique index if not exists salesforce_job_opp_matching_id_uq_idx
    on salesforce_job_opp (matching_id);
