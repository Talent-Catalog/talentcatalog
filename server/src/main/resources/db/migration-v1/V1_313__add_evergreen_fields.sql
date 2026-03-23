
alter table salesforce_job_opp add column evergreen boolean default false not null;
alter table salesforce_job_opp add column evergreen_child_id bigint references salesforce_job_opp;
