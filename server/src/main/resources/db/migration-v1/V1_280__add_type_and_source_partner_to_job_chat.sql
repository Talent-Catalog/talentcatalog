
alter table job_chat add column type text;
alter table job_chat add column source_partner_id bigint references partner;
