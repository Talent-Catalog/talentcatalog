
alter table candidate_role drop column candidate_visa_id;
alter table candidate_role rename to candidate_visa_job_check;
alter table candidate_visa rename to candidate_visa_check;
alter table candidate_visa_job_check add column candidate_visa_check_id bigint references candidate_visa_check;
