
update candidate_dependant set registered = null where registered = 'UNHCRUNRWA';

alter table candidate drop column work_abroad_country_ids;

update candidate set unhcr_status = null where unhcr_status = ' NotRegistered';

alter table candidate drop column unrwa_status;

alter table candidate drop column host_born;
