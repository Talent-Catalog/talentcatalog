
create table candidate_opportunity
(
    id                             bigserial             not null primary key,

    candidate_id                   bigint references candidate,
    closing_comments_for_candidate text,
    employer_feedback              text,
    job_opp_id                     bigint references salesforce_job_opp,
    stage                          text,

--     The following are common with job opportunity
    closing_comments               text,
    closed                         boolean default false not null,
    last_modified_date             timestamptz,
    name                           text,
    next_step                      text,
    next_step_due_date             date,
    sf_id                          text,
    stage_order                    int,

    created_by                     bigint references users,
    created_date                   timestamptz,
    updated_by                     bigint references users,
    updated_date                   timestamptz


);
