
alter table salesforce_job_opp add column created_by bigint references users;
alter table salesforce_job_opp add column created_date timestamptz;
alter table salesforce_job_opp add column published_by bigint references users;
alter table salesforce_job_opp add column published_date timestamptz;
alter table salesforce_job_opp add column updated_by bigint references users;
alter table salesforce_job_opp add column updated_date timestamptz;


