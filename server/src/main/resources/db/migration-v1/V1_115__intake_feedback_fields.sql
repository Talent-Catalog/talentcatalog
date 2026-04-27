
alter table candidate drop column dest_job;
alter table candidate drop column dest_job_notes;

alter table candidate_exam add column notes text;

alter table candidate add column unhcr_not_reg_notes text;

alter table candidate add column work_permit_desired_notes text;

update candidate set returned_home = null where returned_home is not null;

alter table candidate_dependant add column relation_other text;
