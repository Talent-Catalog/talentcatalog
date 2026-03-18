alter table candidate add column asylum_year date;
alter table candidate add column home_location text;

alter table candidate add column unhcr_status text;
alter table candidate add column unhcr_old_status text;
alter table candidate add column unhcr_number text;
alter table candidate add column unhcr_file integer;
alter table candidate add column unhcr_notes text;
alter table candidate add column unhcr_permission text;

alter table candidate add column unrwa_registered text;
alter table candidate add column unrwa_was_registered text;
alter table candidate add column unrwa_number text;
alter table candidate add column unrwa_notes text;

