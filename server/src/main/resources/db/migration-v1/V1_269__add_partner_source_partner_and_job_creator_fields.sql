
alter table partner add column source_partner boolean default false not null;
alter table partner add column job_creator boolean default false not null;

update partner set source_partner = true where partner_type = 'SourcePartner';

update partner set source_partner = true where default_source_partner = true;
update partner set job_creator = true where default_destination_partner = true;

