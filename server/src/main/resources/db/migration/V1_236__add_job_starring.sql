
-- Job starring
create table user_job
(
    user_id bigint not null references users,
    tc_job_id bigint not null references salesforce_job_opp,
    primary key (user_id, tc_job_id)
);

-- Note that the primary key can serve as the index for user_id.
-- See https://stackoverflow.com/questions/3048154/indexes-and-multi-column-primary-keys
create index user_job_id_idx on user_job(tc_job_id);


--Accepting candidates
alter table salesforce_job_opp add column accepting boolean default false not null;

