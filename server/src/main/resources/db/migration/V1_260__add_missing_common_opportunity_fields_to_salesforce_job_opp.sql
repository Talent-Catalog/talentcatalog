
alter table salesforce_job_opp add column closing_comments text;
alter table salesforce_job_opp add column next_step text;
alter table salesforce_job_opp add column next_step_due_date date;
alter table salesforce_job_opp rename column sf_job_opp_id to sf_id;
