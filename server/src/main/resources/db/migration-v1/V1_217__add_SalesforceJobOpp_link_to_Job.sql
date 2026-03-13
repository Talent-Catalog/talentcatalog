
alter table saved_list add column job_id bigint references job;
alter table saved_search add column job_id bigint references job;
