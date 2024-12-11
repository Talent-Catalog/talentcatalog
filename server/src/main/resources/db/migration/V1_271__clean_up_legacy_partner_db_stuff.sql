
alter table partner rename column default_destination_partner to default_job_creator;
alter table partner drop column partner_type;
