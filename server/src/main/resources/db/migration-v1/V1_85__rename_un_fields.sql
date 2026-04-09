
alter table candidate drop column unhcr_status;
alter table candidate drop column unhcr_number;

alter table candidate rename column un_registered to unhcr_status;
alter table candidate rename column un_registration_number to unhcr_number;

alter table candidate rename column unrwa_registered to unrwa_status;
alter table candidate drop column unrwa_was_registered;

alter table candidate alter column unhcr_status type text;

update candidate set unhcr_status = 'RegisteredAsylum' where unhcr_status = 'true';
update candidate set unhcr_status = 'NotRegistered' where unhcr_status = 'false';
