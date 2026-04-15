
alter table partner add column default_destination_partner boolean not null default false;

insert into partner (name, abbreviation, default_destination_partner, partner_type, status)
values ('Talent Beyond Boundaries Dest', 'TBB D', true, 'DefaultDestinationPartner', 'active');

