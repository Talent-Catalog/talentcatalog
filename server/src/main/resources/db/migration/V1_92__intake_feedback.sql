alter table candidate add column partner_edu_level_notes text;
alter table candidate add column partner_occupation_notes text;
alter table candidate add column returned_home_reason_no text;
alter table candidate add column residence_status_notes text;
alter table candidate add column work_desired_notes text;

alter table candidate drop column work_legally;
alter table candidate drop column left_home_reason;
alter table candidate add column left_home_reasons text;
alter table candidate add column military_wanted text;
alter table candidate add column military_notes text;
alter table candidate add column military_start date;
alter table candidate add column military_end date;
alter table candidate add column int_recruit_other text;
alter table candidate add column avail_immediate_job_ops text;
alter table candidate add column unhcr_registered text;
alter table candidate add column unrwa_registered text;
alter table candidate_dependant add column name text;
alter table candidate_dependant add column registered text;

