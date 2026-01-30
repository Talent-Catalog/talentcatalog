alter table candidate_visa_job_check add column candidate_id bigint references candidate;

create index candidate_visa_job_check_candidate_id_idx on candidate_visa_job_check (candidate_id);

update candidate_visa_job_check set candidate_id =
    (select candidate_id from candidate_visa_check where candidate_visa_check_id = id);

alter table candidate_visa_job_check
    alter column candidate_id set not null;
