

update users set partner_id = (select id from partner where default_source_partner = true)
where partner_id is null;

alter table users alter column partner_id set not null;
