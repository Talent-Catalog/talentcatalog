create table if not exists candidate_job_match_explanation (
    candidate_id bigint not null references candidate (id),
    job_id       bigint not null references salesforce_job_opp (id),
    explanation  jsonb  not null,
    created_date timestamptz,
    created_by   bigint references users (id),
    updated_date timestamptz,
    updated_by   bigint references users (id),
    primary key (candidate_id, job_id)
);
