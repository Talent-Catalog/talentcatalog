alter table help_link add column focus text;
alter table help_link add column next_step_name text;
alter table help_link add column next_step_text text;
alter table help_link add column next_step_days integer;

alter table help_link add column created_by bigint references users;
alter table help_link add column created_date timestamptz;
alter table help_link add column updated_by bigint references users;
alter table help_link add column updated_date timestamptz;

