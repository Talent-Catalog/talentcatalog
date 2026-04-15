
create table employer
(
    id                        bigserial not null primary key,

    country_id                bigint references country,
    description               text,
    has_hired_internationally boolean,

    name                      text,
    sf_id                     text,
    website                   text,

    created_by                bigint references users,
    created_date              timestamptz,
    updated_by                bigint references users,
    updated_date              timestamptz
);

alter table salesforce_job_opp add column employer_id bigint references employer;

alter table salesforce_job_opp alter column sf_id drop not null;

alter table partner add column employer_id bigint references employer;

alter table saved_list drop column sf_job_opp_id;
alter table saved_search drop column sf_job_opp_id;
