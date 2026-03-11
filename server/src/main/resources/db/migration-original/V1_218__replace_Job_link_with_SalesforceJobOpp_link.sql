
alter table saved_list drop column job_id;
alter table saved_search drop column job_id;
alter table saved_list add column sf_job_opp_id text references salesforce_job_opp;
alter table saved_search add column sf_job_opp_id text references salesforce_job_opp;
