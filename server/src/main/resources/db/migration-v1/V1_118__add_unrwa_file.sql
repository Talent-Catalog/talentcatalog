
alter table candidate drop column unhcr_not_reg_notes;

alter table candidate add column unrwa_file integer;
alter table candidate add column unrwa_not_reg_status text;
