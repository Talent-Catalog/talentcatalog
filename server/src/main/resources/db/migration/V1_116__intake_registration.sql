
alter table candidate_dependant add column registered_number text;
alter table candidate_dependant add column registered_notes text;
alter table candidate_dependant rename column notes to health_notes;

update candidate set unhcr_old_status = null;
alter table candidate rename column unhcr_old_status to unhcr_not_reg_status;
