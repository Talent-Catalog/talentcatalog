
alter table salesforce_job_opp add column tc_job_id bigserial;
alter table salesforce_job_opp add column submission_due_date timestamptz;
alter table salesforce_job_opp add column submission_list_id bigint references saved_list;


