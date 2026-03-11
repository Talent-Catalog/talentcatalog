
create table salesforce_job_opp
(
    id                        text not null primary key,
    closed                    boolean default false not null,
    country                   text,
    employer                  text,
    last_update               timestamptz,
    name                      text,
    stage                     text,
    stage_order               int
);

create table job
(
    id                        bigserial not null primary key,
    submission_due_date       timestamptz,
    sf_job_opp_id             text references salesforce_job_opp,
    submission_list_id        bigint references saved_list
);

-- To speed up look ups of job by submission list
create index job_submission_list_id_idx on job(submission_list_id);
